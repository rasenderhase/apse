# APSE *AP*pointment *SE*rvice

APSE is a tool to schedule team events. 

APSE starts a survey among the possible attendees and then decides
whether it's worth to start the event or cancel due to lack of participants.

It's specialized to
* _define recurring events_ - for example a weekly soccer game on tuesdays at 8 PM.
* _query all possible attendees_ to confirm or deny their participation - for example at 8 AM.
* _decide if the event takes place_ dependent on the number of comfirmations gathered. For example minimum of 6 confirmations until 4PM.
* _send notifications_ to inform the attendees if the event takes place.

## Technical terms
* participant - person that has confirmed to participate in an event.
* attendees - circle of persons that are allowed to join the event. Possible participants.
* event - instance of an event with a certain start date / time. In an event series (interval in event definition is set up)
  an _event_ is created for each interval.
* event definition - the template of an event.
    * event name
    * start date / time - start of the (first) event
    * interval - optional time interval between events of a series
* query - survey among attendees to confirm or deny their participation at an event.
* decision - determination if number of participants is sufficient to confirm the event.

## Process

1. define event (start date, query duration, decision duration, minimum number of participants, attendees)
2. repeatedly (using a scheduler) select all eventDefinitions with `queryDateTime` (`= startDateTime - durationQueryBeforeEvent`) in the past.
3. for each selected event definition
    1. check if event with same `startDateTime` already exists
    2. create events with `startDateTime`, `attendees`, `eventStatus = INVITATION` and all other values of the definition
    3. if `interval` is set up
        1. update event definition with new start date: `startDateTime = startDateTime + interval`
        2. else delete event definition
4. select all events with `eventStatus = INVITATION`.
5. for each selected event
    1. if (`startDateTime - durationDecisionBeforeEvent` is in the past)
       _decide_
    2. else 
       _invite_
6. wait for confirmations / cancellations
    1. update `attendeeStatus = CONFIRMED / CANCELLED` depending on selected answer in invitation

### invite
1. select all attendees of the event with status `attendeeStatus == IDLE`
2. for each selected attendee: send invitation and set `attendeeStatus = INVITED`

### decide
1. count participants of the event (attendees with `attendeeStatus == CONFIRMED`)
2. if number of participants `>= minimumAttendees`
    1. send confirmation message to all attendees
    2. update `eventStatus = CONFIRMED`
3. else
    1. send cancellation message to all attendees
    2. update `eventStatus = CANCELLED`


## Sample data

### `EventDefinition`
```
{
    "_id": {
        "$oid": "5da38cb084c5a05b1865e4e4"
    },
    "eventName": "Test Event",
    "startDateTime": {
        "$date": "2020-09-21T09:51:30.000Z"
    },
    "interval": "PT4M",
    "durationQueryBeforeEvent": "PT3M",
    "durationDecisionBeforeEvent": "PT1M",
    "queryDateTime": {
        "$date": "2020-09-21T09:48:30.000Z"
    },
    "decisionDateTime": {
        "$date": "2020-09-21T09:50:30.000Z"
    },
    "minimumAttendees": 1,
    "active": true,
    "attendeeDefinitions": [{
        "firstName": "Andi",
        "email": "andreas@knees.de",
        "active": true,
        "attendeeStatus": "IDLE"
    }, {
        "firstName": "Andreas",
        "email": "andreas.nikem@googlemail.com",
        "active": true,
        "attendeeStatus": "IDLE"
    }],
    "_class": "de.nikem.apse.data.entitiy.EventDefinitionEntity"
}
```

### `Event`
```
{
    "_id": {
        "$oid": "5e5450edc3dc4a078ef3d403"
    },
    "eventDefinitionId": "5da38cb084c5a05b1865e4e4",
    "eventName": "Test Event",
    "startDateTime": {
        "$date": "2020-02-24T22:43:30.000Z"
    },
    "queryDateTime": {
        "$date": "2020-02-24T22:40:30.000Z"
    },
    "decisionDateTime": {
        "$date": "2020-02-24T22:42:30.000Z"
    },
    "minimumAttendees": 1,
    "attendees": [{
        "firstName": "Andi",
        "email": "andreas@knees.de",
        "active": true,
        "attendeeStatus": "IDLE"
    }, {
        "firstName": "Andreas",
        "email": "andreas.nikem@googlemail.com",
        "active": true,
        "attendeeStatus": "IDLE"
    }],
    "eventStatus": "INVITATION",
    "_class": "de.nikem.apse.data.entitiy.EventEntity"
}
```
