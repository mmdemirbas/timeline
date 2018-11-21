# oncalls

Tiny Java library to calculate on-call rotations.

Written in pure Java using zero runtime dependency.


## Dependencies

| Dependency Scope    | Dependencies  |
| ------------------- | ------------- |
| Runtime             | only jdk-8    |
| Compile (provided)  | Lombok        |
| Test                | Junit5        |


## Design

![](doc/uml.png)

`Range` is a pair of `Comparable` values which interpreted as start
and end points of a range. This is the simplest data structure in the
library.

`Timeline` is a finite segment of `Comparable` values which allows to
associate its arbitrary sub-segments (`Interval`s) with a list of
arbitrary values. This is the most important and most complex
data structure in the library. It has ~200 lines including comments &
empty lines which is not so complex actually.

`Recurrence` interface represents a periodically recurring interval
of `Comparable` values. Currently, only implementation is
`ZonedDateTimeRecurrence` which is the sole class mentioning `java.time`
package. So, you can easily port whole functionality to another domain
simply by implementing `Recurrence` interface.

`Rotation` assigns a given list of recipients to iterations of a
`Recurrence` one-by-one. This class also adds patch support on top of
those iterations. Patch support is the capability to change recipients
in any part of the Timeline.

`Rotations` is basically a list of `Rotation`s, plus patch support
on top of that.
