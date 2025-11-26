package com.aequitas.aequitascentralservice.adapter.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aequitas.aequitascentralservice.adapter.persistence.entity.CustomerEntity;
import com.aequitas.aequitascentralservice.adapter.persistence.mapper.CustomerMapper;
import com.aequitas.aequitascentralservice.adapter.persistence.repository.CustomerJpaRepository;
import com.aequitas.aequitascentralservice.domain.model.Customer;

@ExtendWith(MockitoExtension.class)
class CustomerRepositoryAdapterTest {

    @Mock
    private CustomerJpaRepository repository;

    private CustomerRepositoryAdapter adapter;

    private UUID testId;
    private UUID testFirmId;
    private String testName;
    private Instant testCreatedAt;

    @BeforeEach
    void setUp() {
        adapter = new CustomerRepositoryAdapter(repository);
        testId = UUID.randomUUID();
        testFirmId = UUID.randomUUID();
        testName = "Test Customer Inc.";
        testCreatedAt = Instant.now();
    }

    @Test
    void GIVEN_existingCustomer_WHEN_findById_THEN_returnsCustomerWrappedInOptional() {
        // GIVEN
        CustomerEntity entity = CustomerEntity.builder()
                .id(testId)
                .firmId(testFirmId)
                .name(testName)
                .createdAt(testCreatedAt)
                .build();

        Customer domainCustomer = Customer.builder()
                .id(testId)
                .firmId(testFirmId)
                .name(testName)
                .createdAt(testCreatedAt)
                .build();

        when(repository.findByIdAndFirmId(testId, testFirmId))
                .thenReturn(Optional.of(entity));

        try (MockedStatic<CustomerMapper> mapperMock = mockStatic(CustomerMapper.class)) {
            mapperMock.when(() -> CustomerMapper.toDomain(entity))
                    .thenReturn(domainCustomer);

            // WHEN
            Optional<Customer> result = adapter.findById(testId, testFirmId);

            // THEN
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(domainCustomer);
            assertThat(result.get().id()).isEqualTo(testId);
            assertThat(result.get().firmId()).isEqualTo(testFirmId);
            assertThat(result.get().name()).isEqualTo(testName);
            assertThat(result.get().createdAt()).isEqualTo(testCreatedAt);

            verify(repository).findByIdAndFirmId(testId, testFirmId);
            verifyNoMoreInteractions(repository);
            mapperMock.verify(() -> CustomerMapper.toDomain(entity));
        }
    }

    @Test
    void GIVEN_noExistingCustomer_WHEN_findById_THEN_returnsEmptyOptional() {
        // GIVEN
        when(repository.findByIdAndFirmId(testId, testFirmId))
                .thenReturn(Optional.empty());

        try (MockedStatic<CustomerMapper> mapperMock = mockStatic(CustomerMapper.class)) {
            // WHEN
            Optional<Customer> result = adapter.findById(testId, testFirmId);

            // THEN
            assertThat(result).isEmpty();

            verify(repository).findByIdAndFirmId(testId, testFirmId);
            verifyNoMoreInteractions(repository);
            mapperMock.verifyNoInteractions();
        }
    }

    @Test
    void GIVEN_differentFirmId_WHEN_findById_THEN_queriesWithCorrectFirmId() {
        // GIVEN
        UUID differentFirmId = UUID.randomUUID();
        when(repository.findByIdAndFirmId(testId, differentFirmId))
                .thenReturn(Optional.empty());

        try (MockedStatic<CustomerMapper> mapperMock = mockStatic(CustomerMapper.class)) {
            // WHEN
            Optional<Customer> result = adapter.findById(testId, differentFirmId);

            // THEN
            assertThat(result).isEmpty();

            verify(repository).findByIdAndFirmId(testId, differentFirmId);
            verifyNoMoreInteractions(repository);
            mapperMock.verifyNoInteractions();
        }
    }

    @Test
    void GIVEN_differentCustomerId_WHEN_findById_THEN_queriesWithCorrectId() {
        // GIVEN
        UUID differentId = UUID.randomUUID();
        when(repository.findByIdAndFirmId(differentId, testFirmId))
                .thenReturn(Optional.empty());

        try (MockedStatic<CustomerMapper> mapperMock = mockStatic(CustomerMapper.class)) {
            // WHEN
            Optional<Customer> result = adapter.findById(differentId, testFirmId);

            // THEN
            assertThat(result).isEmpty();

            verify(repository).findByIdAndFirmId(differentId, testFirmId);
            verifyNoMoreInteractions(repository);
            mapperMock.verifyNoInteractions();
        }
    }

    @Test
    void GIVEN_customerWithLongName_WHEN_findById_THEN_returnsCustomerWithFullName() {
        // GIVEN
        String longName = "Very Long Customer Name Corporation International Limited Partnership LLC";
        CustomerEntity entity = CustomerEntity.builder()
                .id(testId)
                .firmId(testFirmId)
                .name(longName)
                .createdAt(testCreatedAt)
                .build();

        Customer domainCustomer = Customer.builder()
                .id(testId)
                .firmId(testFirmId)
                .name(longName)
                .createdAt(testCreatedAt)
                .build();

        when(repository.findByIdAndFirmId(testId, testFirmId))
                .thenReturn(Optional.of(entity));

        try (MockedStatic<CustomerMapper> mapperMock = mockStatic(CustomerMapper.class)) {
            mapperMock.when(() -> CustomerMapper.toDomain(entity))
                    .thenReturn(domainCustomer);

            // WHEN
            Optional<Customer> result = adapter.findById(testId, testFirmId);

            // THEN
            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo(longName);
            assertThat(result.get().name()).hasSize(longName.length());

            verify(repository).findByIdAndFirmId(testId, testFirmId);
            verifyNoMoreInteractions(repository);
            mapperMock.verify(() -> CustomerMapper.toDomain(entity));
        }
    }

    @Test
    void GIVEN_multipleCallsWithSameParameters_WHEN_findById_THEN_queriesRepositoryEachTime() {
        // GIVEN
        CustomerEntity entity = CustomerEntity.builder()
                .id(testId)
                .firmId(testFirmId)
                .name(testName)
                .createdAt(testCreatedAt)
                .build();

        Customer domainCustomer = Customer.builder()
                .id(testId)
                .firmId(testFirmId)
                .name(testName)
                .createdAt(testCreatedAt)
                .build();

        when(repository.findByIdAndFirmId(testId, testFirmId))
                .thenReturn(Optional.of(entity));

        try (MockedStatic<CustomerMapper> mapperMock = mockStatic(CustomerMapper.class)) {
            mapperMock.when(() -> CustomerMapper.toDomain(entity))
                    .thenReturn(domainCustomer);

            // WHEN
            Optional<Customer> result1 = adapter.findById(testId, testFirmId);
            Optional<Customer> result2 = adapter.findById(testId, testFirmId);

            // THEN
            assertThat(result1).isPresent();
            assertThat(result2).isPresent();
            assertThat(result1.get()).isEqualTo(domainCustomer);
            assertThat(result2.get()).isEqualTo(domainCustomer);

            verify(repository, times(2)).findByIdAndFirmId(testId, testFirmId);
            verifyNoMoreInteractions(repository);
            mapperMock.verify(() -> CustomerMapper.toDomain(entity), times(2));
        }
    }
}
