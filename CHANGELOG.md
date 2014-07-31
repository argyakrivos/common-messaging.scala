## 0.5.0 ([#9](https://git.mobcastdev.com/Hermes/common-messaging/pull/9) 2014-07-30 15:17:11)

Adds support for strongly typed JSON event bodies

### Breaking changes

- The `Event.json` helper method has changed from expecting a `String` to expecting an object to serialise.
- The `JsonContentType` field has been removed.

### New features

- Added a `MediaType` class to represent media types.
- Added the ability to create JSON messages from serialised objects.

### Bug fixes

- Event headers now always use the UTC time zone.

## 0.4.0 ([#8](https://git.mobcastdev.com/Hermes/common-messaging/pull/8) 2014-07-17 09:03:58)

CP-1567: Changed handleEvent function to return Future[Unit] and  change...

breaking change
changed the handleEvent function signature back to original version


## 0.3.0 ([#7](https://git.mobcastdev.com/Hermes/common-messaging/pull/7) 2014-07-16 13:28:04)

CP-1567: Changed handleEvent function to return Try[Future]

breaking change
handleEvent function signature change

## 0.2.1 ([#5](https://git.mobcastdev.com/Hermes/common-messaging/pull/5) 2014-07-02 11:27:19)

Fix logging

Patch to fix logging so that exception details are logged correctly.

## 0.2.0 ([#4](https://git.mobcastdev.com/Hermes/common-messaging/pull/4) 2014-06-30 17:15:01)

Simplified API

#### New features

- Removed MessagePublisher interface.
- Added simple implementation of ErrorHandler that wraps a publisher Actor.


## 0.1.0 ([#3](https://git.mobcastdev.com/Hermes/common-messaging/pull/3) 2014-06-24 09:49:09)

Added ID field in events.

### New features 

- Added ID field in events, used for unique message IDs to aid in message tracking.


## 0.1.0

Added basic definition of common message types and formats.

