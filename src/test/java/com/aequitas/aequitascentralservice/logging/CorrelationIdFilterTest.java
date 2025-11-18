package com.aequitas.aequitascentralservice.logging;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tests for {@link CorrelationIdFilter}.
 */
@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String TEST_CORRELATION_ID = "test-correlation-123";

    @Captor
    private ArgumentCaptor<String> correlationIdCaptor;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private CorrelationIdFilter filter;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void GIVEN_requestWithCorrelationIdHeader_WHEN_doFilterInternal_THEN_useExistingCorrelationId()
            throws ServletException, IOException {
        // GIVEN
        when(request.getHeader(CORRELATION_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // WHEN
        filter.doFilterInternal(request, response, filterChain);

        // THEN
        verify(request, times(2)).getHeader(CORRELATION_HEADER);
        verify(response, times(1)).addHeader(CORRELATION_HEADER, TEST_CORRELATION_ID);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(MDC.get(CORRELATION_HEADER));
        verifyNoMoreInteractions(request, response, filterChain);
    }

    @Test
    void GIVEN_requestWithoutCorrelationIdHeader_WHEN_doFilterInternal_THEN_generateNewCorrelationId()
            throws ServletException, IOException {
        // GIVEN
        when(request.getHeader(CORRELATION_HEADER)).thenReturn(null);

        // WHEN
        filter.doFilterInternal(request, response, filterChain);

        // THEN
        verify(request, times(1)).getHeader(CORRELATION_HEADER);
        verify(response, times(1)).addHeader(eq(CORRELATION_HEADER), correlationIdCaptor.capture());
        String capturedCorrelationId = correlationIdCaptor.getValue();
        assertNotNull(capturedCorrelationId);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(MDC.get(CORRELATION_HEADER));
        verifyNoMoreInteractions(request, response, filterChain);
    }

    @Test
    void GIVEN_requestWithCorrelationIdHeader_WHEN_doFilterInternal_THEN_setMdcDuringFilterChain()
            throws ServletException, IOException {
        // GIVEN
        when(request.getHeader(CORRELATION_HEADER)).thenReturn(TEST_CORRELATION_ID);
        String[] mdcValueDuringChain = new String[1];
        doThrow(new RuntimeException("Test exception") {
            @Override
            public synchronized Throwable fillInStackTrace() {
                mdcValueDuringChain[0] = MDC.get(CORRELATION_HEADER);
                return super.fillInStackTrace();
            }
        }).when(filterChain).doFilter(request, response);

        // WHEN
        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException e) {
            // Expected exception
        }

        // THEN
        assertEquals(TEST_CORRELATION_ID, mdcValueDuringChain[0]);
        assertNull(MDC.get(CORRELATION_HEADER));
        verify(request, times(2)).getHeader(CORRELATION_HEADER);
        verify(response, times(1)).addHeader(CORRELATION_HEADER, TEST_CORRELATION_ID);
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoMoreInteractions(request, response, filterChain);
    }

    @Test
    void GIVEN_filterChainThrowsServletException_WHEN_doFilterInternal_THEN_cleanupMdcAndRethrow()
            throws ServletException, IOException {
        // GIVEN
        when(request.getHeader(CORRELATION_HEADER)).thenReturn(TEST_CORRELATION_ID);
        ServletException expectedException = new ServletException("Filter chain failed");
        doThrow(expectedException).when(filterChain).doFilter(request, response);

        // WHEN
        ServletException exception = null;
        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (ServletException e) {
            exception = e;
        }

        // THEN
        assertEquals(expectedException, exception);
        assertNull(MDC.get(CORRELATION_HEADER));
        verify(request, times(2)).getHeader(CORRELATION_HEADER);
        verify(response, times(1)).addHeader(CORRELATION_HEADER, TEST_CORRELATION_ID);
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoMoreInteractions(request, response, filterChain);
    }

    @Test
    void GIVEN_filterChainThrowsIOException_WHEN_doFilterInternal_THEN_cleanupMdcAndRethrow()
            throws ServletException, IOException {
        // GIVEN
        when(request.getHeader(CORRELATION_HEADER)).thenReturn(TEST_CORRELATION_ID);
        IOException expectedException = new IOException("I/O error");
        doThrow(expectedException).when(filterChain).doFilter(request, response);

        // WHEN
        IOException exception = null;
        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (IOException e) {
            exception = e;
        }

        // THEN
        assertEquals(expectedException, exception);
        assertNull(MDC.get(CORRELATION_HEADER));
        verify(request, times(2)).getHeader(CORRELATION_HEADER);
        verify(response, times(1)).addHeader(CORRELATION_HEADER, TEST_CORRELATION_ID);
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoMoreInteractions(request, response, filterChain);
    }

    @Test
    void GIVEN_filterChainThrowsRuntimeException_WHEN_doFilterInternal_THEN_cleanupMdcAndRethrow()
            throws ServletException, IOException {
        // GIVEN
        when(request.getHeader(CORRELATION_HEADER)).thenReturn(TEST_CORRELATION_ID);
        RuntimeException expectedException = new RuntimeException("Unexpected error");
        doThrow(expectedException).when(filterChain).doFilter(request, response);

        // WHEN
        RuntimeException exception = null;
        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException e) {
            exception = e;
        }

        // THEN
        assertEquals(expectedException, exception);
        assertNull(MDC.get(CORRELATION_HEADER));
        verify(request, times(2)).getHeader(CORRELATION_HEADER);
        verify(response, times(1)).addHeader(CORRELATION_HEADER, TEST_CORRELATION_ID);
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoMoreInteractions(request, response, filterChain);
    }

    @Test
    void GIVEN_emptyCorrelationIdHeader_WHEN_doFilterInternal_THEN_useEmptyString()
            throws ServletException, IOException {
        // GIVEN
        String emptyCorrelationId = "";
        when(request.getHeader(CORRELATION_HEADER)).thenReturn(emptyCorrelationId);

        // WHEN
        filter.doFilterInternal(request, response, filterChain);

        // THEN
        verify(request, times(2)).getHeader(CORRELATION_HEADER);
        verify(response, times(1)).addHeader(CORRELATION_HEADER, emptyCorrelationId);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(MDC.get(CORRELATION_HEADER));
        verifyNoMoreInteractions(request, response, filterChain);
    }

    @Test
    void GIVEN_multipleRequests_WHEN_doFilterInternal_THEN_mdcCleanedBetweenRequests()
            throws ServletException, IOException {
        // GIVEN
        when(request.getHeader(CORRELATION_HEADER)).thenReturn(TEST_CORRELATION_ID);

        // WHEN - First request
        filter.doFilterInternal(request, response, filterChain);

        // THEN - MDC cleaned after first request
        assertNull(MDC.get(CORRELATION_HEADER));

        // WHEN - Second request
        filter.doFilterInternal(request, response, filterChain);

        // THEN - MDC cleaned after second request
        assertNull(MDC.get(CORRELATION_HEADER));
        verify(request, times(4)).getHeader(CORRELATION_HEADER);
        verify(response, times(2)).addHeader(CORRELATION_HEADER, TEST_CORRELATION_ID);
        verify(filterChain, times(2)).doFilter(request, response);
        verifyNoMoreInteractions(request, response, filterChain);
    }
}
