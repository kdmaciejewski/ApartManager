package com.inzynierka2k24.apiserver.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.inzynierka2k24.ExternalService;
import com.inzynierka2k24.ResponseStatus;
import com.inzynierka2k24.ServiceResponse;
import com.inzynierka2k24.apiserver.exception.apartment.ApartmentNotFoundException;
import com.inzynierka2k24.apiserver.exception.reservation.ReservationNotFoundException;
import com.inzynierka2k24.apiserver.grpc.integration.ExternalIntegrationServiceClient;
import com.inzynierka2k24.apiserver.model.Apartment;
import com.inzynierka2k24.apiserver.model.ExternalAccount;
import com.inzynierka2k24.apiserver.model.Reservation;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class IntegrationServiceTest {

  private final ExternalIntegrationServiceClient client =
      mock(ExternalIntegrationServiceClient.class);
  private final ExternalAccountService accountService = mock(ExternalAccountService.class);
  private final ReservationService reservationService = mock(ReservationService.class);
  private final ApartmentService apartmentService = mock(ApartmentService.class);
  private final IntegrationService integrationService =
      new IntegrationService(client, accountService, reservationService, apartmentService);

  @Test
  public void shouldPropagateReservationValidIdsReturnsMap() throws ReservationNotFoundException {
    // Given
    long userId = 1;
    long apartmentId = 1;
    long reservationId = 1;

    Reservation reservation =
        new Reservation(Optional.of(1L), 1L, Instant.now(), Instant.now().plusSeconds(3600));
    List<ExternalAccount> accounts = List.of(new ExternalAccount(1L, "login", "password", 1));
    List<ServiceResponse> responses =
        List.of(
            ServiceResponse.newBuilder()
                .setService(ExternalService.BOOKING)
                .setStatus(ResponseStatus.SUCCESS)
                .build());

    when(reservationService.getById(apartmentId, reservationId)).thenReturn(reservation);
    when(accountService.getAll(userId)).thenReturn(accounts);
    when(client.propagateReservation(reservation, accounts)).thenReturn(responses);

    // When
    Map<String, String> result =
        integrationService.propagateReservation(userId, apartmentId, reservationId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
  }

  @Test
  public void shouldGetReservationsValidUserAndTimeRangeReturnsList() {
    // Given
    long userId = 1;
    Instant from = Instant.now();
    Instant to = Instant.now().plus(Duration.ofDays(7));
    List<ExternalAccount> accounts = List.of(new ExternalAccount(1L, "login", "password", 1));

    when(accountService.getAll(userId)).thenReturn(accounts);
    when(client.getReservations(from, to, accounts))
        .thenReturn(
            List.of(
                new Reservation(
                    Optional.of(1L), 1L, Instant.now(), Instant.now().plusSeconds(3600))));

    // When
    List<com.inzynierka2k24.Reservation> result =
        integrationService.getReservations(userId, from, to);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).isEmpty(); // TODO Change after implementing this method
  }

  @Test
  public void shouldUpdateApartmentDetailsValidUserAndApartmentIdsReturnsList()
      throws ApartmentNotFoundException {
    // Given
    long userId = 1;
    long apartmentId = 1;

    Apartment apartment =
        new Apartment(
            Optional.of(1L), 100.0f, "Title", "Country", "City", "Street", "Building", "Apartment");
    List<ExternalAccount> accounts = List.of(new ExternalAccount(1L, "login", "password", 1));
    List<ServiceResponse> responses =
        List.of(
            ServiceResponse.newBuilder()
                .setService(ExternalService.BOOKING)
                .setStatus(ResponseStatus.SUCCESS)
                .build());

    when(apartmentService.getById(userId, apartmentId)).thenReturn(apartment);
    when(accountService.getAll(userId)).thenReturn(accounts);
    when(client.updateApartmentDetails(apartment, accounts)).thenReturn(responses);

    // When
    Map<String, String> result = integrationService.updateApartmentDetails(userId, apartmentId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
  }

  @Test
  public void shouldPropagateReservationInvalidReservationIdThrowsException()
      throws ReservationNotFoundException {
    // Given
    long userId = 1;
    long apartmentId = 1;
    long reservationId = 1;

    when(reservationService.getById(apartmentId, reservationId))
        .thenThrow(ReservationNotFoundException.class);

    // When/Then
    assertThatThrownBy(() -> integrationService.propagateReservation(userId, apartmentId, reservationId))
        .isInstanceOf(ReservationNotFoundException.class);
  }

  @Test
  public void shouldGetReservationsInvalidUserIdReturnsEmptyList() {
    // Given
    long userId = 1;
    Instant from = Instant.now();
    Instant to = Instant.now().plus(Duration.ofDays(7));

    when(accountService.getAll(userId)).thenReturn(Collections.emptyList());

    // When
    List<com.inzynierka2k24.Reservation> result =
        integrationService.getReservations(userId, from, to);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldUpdateApartmentDetailsInvalidApartmentIdThrowsException()
      throws ApartmentNotFoundException {
    // Given
    long userId = 1;
    long apartmentId = 1;

    when(apartmentService.getById(userId, apartmentId)).thenThrow(ApartmentNotFoundException.class);

    // When/Then
    assertThatThrownBy(() -> integrationService.updateApartmentDetails(userId, apartmentId))
        .isInstanceOf(ApartmentNotFoundException.class);
  }

  @Test
  public void shouldPropagateReservationNoAssociatedAccountsReturnsEmptyMap()
      throws ReservationNotFoundException {
    // Given
    long userId = 1;
    long apartmentId = 1;
    long reservationId = 1;

    Reservation reservation =
        new Reservation(Optional.of(1L), 1L, Instant.now(), Instant.now().plusSeconds(3600));

    when(reservationService.getById(apartmentId, reservationId)).thenReturn(reservation);
    when(accountService.getAll(userId)).thenReturn(Collections.emptyList());

    // When
    Map<String, String> result =
        integrationService.propagateReservation(userId, apartmentId, reservationId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }
}
