# Change Log

## 1.1.3 ([#22](https://git.mobcastdev.com/Hermes/common-messaging/pull/22) 2014-09-04 13:06:48)

Cross compiles to Scala 2.11

### Improvements

* Now cross-compiles to Scala 2.11

## 1.1.2 ([#20](https://git.mobcastdev.com/Hermes/common-messaging/pull/20) 2014-08-20 11:24:59)

Fixed comments.

### Improvements.

- Fixed error in comments for abstract method in `ReliableEventHandler`.


## 1.1.1 ([#19](https://git.mobcastdev.com/Hermes/common-messaging/pull/19) 2014-08-11 17:30:14)

Fixed logging, warnings.

### Bug fixes:

- Fixed logging message.
- Removed compiler warnings.


## 1.1.0 ([#17](https://git.mobcastdev.com/Hermes/common-messaging/pull/17) 2014-08-07 15:30:59)

XML schema validation & XML convenience methods

### New features

- XML validator
- More XML convenience methods for `Int`s and Joda `DateTime`s

## 1.0.1 ([#16](https://git.mobcastdev.com/Hermes/common-messaging/pull/16) 2014-08-07 09:35:05)

Restrict access to protected members.

### Bug fix

- Tighten access to protected methods, to make it more clear how they're intended to be used, and avoid accidental incorrect use.


## 1.0.0 (2014-08-06 16:57:34)

Manually bumped the version to 1.0.0.

## 0.7.0 ([#13](https://git.mobcastdev.com/Hermes/common-messaging/pull/13) 2014-08-06 15:47:55)

Updated to latest version of scala-logging

### Breaking changes

- Now uses v2.x of the scala-logging library.

## 0.6.2 ([#12](https://git.mobcastdev.com/Hermes/common-messaging/pull/12) 2014-08-06 12:30:23)

Replaced some JSON code with common-json

### Improvements

- Now uses code in common-json rather than having duplicated code in
multiple projects.

## 0.6.1 ([#11](https://git.mobcastdev.com/Hermes/common-messaging/pull/11) 2014-08-05 10:52:50)

CP-1563: Added serialization/de-serialization support for java.net.URI

improvement

## 0.6.0 ([#10](https://git.mobcastdev.com/Hermes/common-messaging/pull/10) 2014-08-01 14:33:34)

Added an inferred $schema field to messages

### Breaking changes

- Media types must follow the pattern
`application/vnd.blinkbox.books.{schemaName}+json` or they cannot be
published using `JsonMessageBody`.

### New features

- Now adds a `$schema` field to all messages, with a schema name
inferred from the media type.

### Bug fixes

- Deserialised `DateTime` instances from JSON messages are now always
in the UTC rather than local time zone.

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

