package com.aequitas.aequitascentralservice.tenancy;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;

import com.aequitas.aequitascentralservice.tenancy.datasource.TenantAwareDataSource;

@ExtendWith(MockitoExtension.class)
class TenancyConfigTest {

    @Mock
    private DataSourceProperties mockDataSourceProperties;

    @Mock
    private DataSource mockDataSource;

    private TenancyConfig tenancyConfig;

    @BeforeEach
    void setUp() {
        tenancyConfig = new TenancyConfig();
    }

    @Test
    void GIVEN_DataSourceProperties_WHEN_DataSourceCalled_THEN_ReturnNonNullDataSource() {
        // GIVEN
        final DataSourceBuilder<?> mockDataSourceBuilder = mock(DataSourceBuilder.class);
        when(mockDataSourceProperties.initializeDataSourceBuilder()).thenAnswer((Answer<DataSourceBuilder<?>>) invocation -> mockDataSourceBuilder);
        when(mockDataSourceBuilder.build()).thenAnswer((Answer<DataSource>) invocation -> mockDataSource);

        // WHEN
        final DataSource result = tenancyConfig.dataSource(mockDataSourceProperties);

        // THEN
        assertNotNull(result, "DataSource should not be null");
        assertSame(mockDataSource, result, "DataSource should be the built instance from properties");
        verify(mockDataSourceProperties, times(1)).initializeDataSourceBuilder();
        verify(mockDataSourceBuilder, times(1)).build();
        verifyNoMoreInteractions(mockDataSourceProperties, mockDataSourceBuilder);
    }

    @Test
    void GIVEN_NullDataSourceProperties_WHEN_DataSourceCalled_THEN_ThrowNullPointerException() {
        // GIVEN
        final DataSourceProperties nullProperties = null;

        // WHEN & THEN
        try {
            tenancyConfig.dataSource(nullProperties);
            throw new AssertionError("Expected NullPointerException to be thrown");
        } catch (final NullPointerException e) {
            // Expected exception
            assertNotNull(e, "NullPointerException should be thrown");
        }
    }

    @Test
    void GIVEN_DelegateDataSource_WHEN_TenantAwareDataSourceCalled_THEN_ReturnNonNullTenantAwareDataSource() {
        // GIVEN
        // mockDataSource is initialized in setUp

        // WHEN
        final DataSource result = tenancyConfig.tenantAwareDataSource(mockDataSource);

        // THEN
        assertNotNull(result, "TenantAwareDataSource should not be null");
        assertTrue(result instanceof TenantAwareDataSource, 
                "Result should be an instance of TenantAwareDataSource");
        verifyNoMoreInteractions(mockDataSource);
    }

    @Test
    void GIVEN_NullDelegateDataSource_WHEN_TenantAwareDataSourceCalled_THEN_ReturnTenantAwareDataSourceWithNullDelegate() {
        // GIVEN
        final DataSource nullDelegate = null;

        // WHEN
        final DataSource result = tenancyConfig.tenantAwareDataSource(nullDelegate);

        // THEN
        assertNotNull(result, "TenantAwareDataSource should not be null even with null delegate");
        assertTrue(result instanceof TenantAwareDataSource, 
                "Result should be an instance of TenantAwareDataSource");
    }

    @Test
    void GIVEN_MultipleCalls_WHEN_DataSourceCalled_THEN_ReturnBuiltDataSourceEachTime() {
        // GIVEN
        final DataSource mockDataSource1 = mock(DataSource.class);
        final DataSource mockDataSource2 = mock(DataSource.class);
        final DataSourceBuilder<?> mockBuilder1 = mock(DataSourceBuilder.class);
        final DataSourceBuilder<?> mockBuilder2 = mock(DataSourceBuilder.class);

        when(mockDataSourceProperties.initializeDataSourceBuilder())
                .thenAnswer((Answer<DataSourceBuilder<?>>) invocation -> mockBuilder1)
                .thenAnswer((Answer<DataSourceBuilder<?>>) invocation -> mockBuilder2);
        when(mockBuilder1.build()).thenAnswer((Answer<DataSource>) invocation -> mockDataSource1);
        when(mockBuilder2.build()).thenAnswer((Answer<DataSource>) invocation -> mockDataSource2);

        // WHEN
        final DataSource result1 = tenancyConfig.dataSource(mockDataSourceProperties);
        final DataSource result2 = tenancyConfig.dataSource(mockDataSourceProperties);

        // THEN
        assertNotNull(result1, "First DataSource should not be null");
        assertNotNull(result2, "Second DataSource should not be null");
        assertSame(mockDataSource1, result1, "First result should be mockDataSource1");
        assertSame(mockDataSource2, result2, "Second result should be mockDataSource2");
        verify(mockDataSourceProperties, times(2)).initializeDataSourceBuilder();
        verify(mockBuilder1, times(1)).build();
        verify(mockBuilder2, times(1)).build();
        verifyNoMoreInteractions(mockDataSourceProperties, mockBuilder1, mockBuilder2);
    }

    @Test
    void GIVEN_MultipleCalls_WHEN_TenantAwareDataSourceCalled_THEN_ReturnNewInstanceEachTime() {
        // GIVEN
        final DataSource mockDelegate1 = mock(DataSource.class);
        final DataSource mockDelegate2 = mock(DataSource.class);

        // WHEN
        final DataSource result1 = tenancyConfig.tenantAwareDataSource(mockDelegate1);
        final DataSource result2 = tenancyConfig.tenantAwareDataSource(mockDelegate2);

        // THEN
        assertNotNull(result1, "First TenantAwareDataSource should not be null");
        assertNotNull(result2, "Second TenantAwareDataSource should not be null");
        assertTrue(result1 instanceof TenantAwareDataSource, 
                "First result should be TenantAwareDataSource");
        assertTrue(result2 instanceof TenantAwareDataSource, 
                "Second result should be TenantAwareDataSource");
        assertTrue(result1 != result2, "Each call should return a new instance");
        verifyNoMoreInteractions(mockDelegate1, mockDelegate2);
    }
}
