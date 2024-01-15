package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServicesImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    @Test
    void addParticipant() {
        // Create a participant for testing
        Participant participant = new Participant();
        participant.setIdPart(1);
        participant.setNom("John");
        participant.setPrenom("Doe");

        // Mock the repository save method
        when(participantRepository.save(any())).thenReturn(participant);

        // Call the method to be tested
        Participant savedParticipant = eventServices.addParticipant(participant);

        // Verify that the repository save method was called with the correct argument
        verify(participantRepository, times(1)).save(participant);

        // Assert the result
        assertNotNull(savedParticipant);
        assertEquals(participant.getIdPart(), savedParticipant.getIdPart());
        assertEquals(participant.getNom(), savedParticipant.getNom());
        assertEquals(participant.getPrenom(), savedParticipant.getPrenom());
    }


    @Test
    void addAffectEvenParticipantById() {
        // Create an event and a participant for testing
        Event event = new Event();
        event.setDescription("Test Event");
        int participantId = 1;

        Participant participant = new Participant();
        participant.setIdPart(participantId);

        // Mock the repository findById method
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));

        // Mock the repository save method
        when(eventRepository.save(any())).thenAnswer(invocation -> {
            Event savedEventArgument = invocation.getArgument(0);
            // Simulate the behavior of saving the event in the repository
            savedEventArgument.setIdEvent(1); // Assuming setId exists in your Event class
            return savedEventArgument;
        });

        // Call the method to be tested
        Event savedEvent = eventServices.addAffectEvenParticipant(event, participantId);

        // Verify that the repository findById method was called with the correct argument
        verify(participantRepository, times(1)).findById(participantId);

        // Verify that the repository save method was called with the correct argument
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, times(1)).save(eventCaptor.capture());

        // Assert the result
        assertNotNull(savedEvent);
        assertTrue(savedEvent.getParticipants().contains(participant));

        // Assert additional properties of the saved event
        Event capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        // Add more assertions as needed
    }




    @Test
    void addAffectEvenParticipantByParticipants() {
        // Create an event with participants for testing
        Event event = new Event();
        event.setDescription("Test Event");

        Set<Participant> participants = new HashSet<>();
        Participant participant1 = new Participant();
        participant1.setIdPart(1);
        Participant participant2 = new Participant();
        participant2.setIdPart(2);

        participants.add(participant1);
        participants.add(participant2);

        event.setParticipants(participants);

        // Mock the repository findById and save methods
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant1));
        when(participantRepository.findById(2)).thenReturn(Optional.of(participant2));
        when(eventRepository.save(any())).thenReturn(event);

        // Call the method to be tested
        Event savedEvent = eventServices.addAffectEvenParticipant(event);

        // Verify that the repository findById and save methods were called with the correct arguments
        verify(participantRepository, times(1)).findById(1);
        verify(participantRepository, times(1)).findById(2);
        verify(eventRepository, times(1)).save(event);

        // Assert the result
        assertNotNull(savedEvent);
        assertTrue(savedEvent.getParticipants().contains(participant1));
        assertTrue(savedEvent.getParticipants().contains(participant2));
    }

    @Test
    void addAffectLog() {
        // Create logistics and an event description for testing
        Logistics logistics = new Logistics();
        logistics.setDescription("Test Logistics");
        logistics.setReserve(true);
        logistics.setPrixUnit(10.0f);
        logistics.setQuantite(5);
        String eventDescription = "Test Event";

        Event event = new Event();
        event.setDescription(eventDescription);

        // Mock the repository findByDescription and save methods
        when(eventRepository.findByDescription(eventDescription)).thenReturn(event);
        when(logisticsRepository.save(any())).thenReturn(logistics);

        // Call the method to be tested
        Logistics savedLogistics = eventServices.addAffectLog(logistics, eventDescription);

        // Verify that the repository findByDescription and save methods were called with the correct arguments
        verify(eventRepository, times(1)).findByDescription(eventDescription);
        verify(logisticsRepository, times(1)).save(logistics);

        // Assert the result
        assertNotNull(savedLogistics);
        assertTrue(event.getLogistics().contains(logistics));
    }

    @Test
    void getLogisticsDates() {
        // Create a date range for testing
        LocalDate date_debut = LocalDate.of(2023, 1, 1);
        LocalDate date_fin = LocalDate.of(2023, 12, 31);

        // Create events with logistics for testing
        Event event1 = new Event();
        event1.setDescription("Event 1");
        Logistics logistics1 = new Logistics();
        logistics1.setDescription("Logistics 1");
        logistics1.setReserve(true);
        logistics1.setPrixUnit(10.0f);
        logistics1.setQuantite(5);
        event1.getLogistics().add(logistics1);

        Event event2 = new Event();
        event2.setDescription("Event 2");
        Logistics logistics2 = new Logistics();
        logistics2.setDescription("Logistics 2");
        logistics2.setReserve(true);
        logistics2.setPrixUnit(8.0f);
        logistics2.setQuantite(3);
        event2.getLogistics().add(logistics2);

        // logistics2 is not reserved, so it should not be included
        event2.getLogistics().add(logistics2);

        // Mock the repository findByDateDebutBetween method
        when(eventRepository.findByDateDebutBetween(date_debut, date_fin))
                .thenReturn(Arrays.asList(event1, event2));  // Use Arrays.asList


        // Call the method to be tested
        List<Logistics> logisticsList = eventServices.getLogisticsDates(date_debut, date_fin);

        // Verify that the repository findByDateDebutBetween method was called with the correct arguments
        verify(eventRepository, times(1)).findByDateDebutBetween(date_debut, date_fin);

        // Assert the result
        assertNotNull(logisticsList);
        assertEquals(1, logisticsList.size());
        assertTrue(logisticsList.contains(logistics1));
        assertFalse(logisticsList.contains(logistics2));
    }

    @Test
    void calculCout() {
        // Create events and participants for testing
        Event event1 = new Event();
        event1.setDescription("Event 1");
        Participant participant1 = new Participant();
        participant1.setNom("Tounsi");
        participant1.setPrenom("Ahmed");
        participant1.setTache(Tache.ORGANISATEUR);
        event1.getParticipants().add(participant1);

        Event event2 = new Event();
        event2.setDescription("Event 2");
        Participant participant2 = new Participant();
        participant2.setNom("Another");
        participant2.setPrenom("Participant");
        participant2.setTache(Tache.SERVEUR);
        event2.getParticipants().add(participant2);

        // Create reserved logistics for events
        Logistics logistics1 = new Logistics();
        logistics1.setDescription("Logistics 1");
        logistics1.setReserve(true);
        logistics1.setPrixUnit(10.0f);
        logistics1.setQuantite(5);
        event1.getLogistics().add(logistics1);

        Logistics logistics2 = new Logistics();
        logistics2.setDescription("Logistics 2");
        logistics2.setReserve(false); // not reserved
        logistics2.setPrixUnit(8.0f);
        logistics2.setQuantite(3);
        event2.getLogistics().add(logistics2);

        // Mock the repository findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache and save methods
        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache("Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(Arrays.asList(event1, event2));
        when(eventRepository.save(any())).thenReturn(event1, event2);

        // Call the method to be tested
        eventServices.calculCout();

        // Verify that the repository findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache and save methods were called
        verify(eventRepository, times(1))
                .findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache("Tounsi", "Ahmed", Tache.ORGANISATEUR);
        verify(eventRepository, times(2)).save(any());

        // Assert the results
        assertEquals(50.0f, event1.getCout()); // 10.0 * 5
        assertEquals(0.0f, event2.getCout()); // not reserved logistics
    }
}
