package org.itmo.testing.lab2.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserStatusServiceTest {

    private UserAnalyticsService userAnalyticsService;
    private UserStatusService userStatusService;

    @BeforeAll
    void setUp() {
        userAnalyticsService = mock(UserAnalyticsService.class);
        userStatusService = new UserStatusService(userAnalyticsService);
    }

    @Test
    public void testGetUserStatus_Active() {
        // Настроим поведение mock-объекта
        when(userAnalyticsService.getTotalActivityTime("user123")).thenReturn(90L);

        String status = userStatusService.getUserStatus("user123");

        assertEquals("Active", status);
    }

    @Test
    public void testGetUserStatus_Inactive() {
        when(userAnalyticsService.getTotalActivityTime("user123")).thenReturn(30L);
        String status = userStatusService.getUserStatus("user123");
        assertEquals("Inactive", status);
    }

    @Test
    public void testGetUserStatus_HighlyActive() {
        when(userAnalyticsService.getTotalActivityTime("user123")).thenReturn(150L);
        String status = userStatusService.getUserStatus("user123");
        assertEquals("Highly active", status);
    }

    @Test
    public void testGetUserLastSessionDate() {
        LinkedList<UserAnalyticsService.Session> sessions = new LinkedList<>();
        LocalDateTime logoutTime = LocalDateTime.of(2024, 5, 20, 10, 0);
        sessions.add(new UserAnalyticsService.Session(LocalDateTime.of(2024, 5, 20, 9, 0), logoutTime));

        when(userAnalyticsService.getUserSessions("user123")).thenReturn(sessions);

        Optional<String> result = userStatusService.getUserLastSessionDate("user123");
        assertTrue(result.isPresent());
        assertEquals(LocalDate.of(2024, 5, 20).toString(), result.get());

        verify(userAnalyticsService).getUserSessions("user123");
    }

}
