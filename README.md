<!--
Copyright © 2011-2012, Jeremy Heiner (github.com/JHeiner). All rights reserved.
Copying and distribution of this file, with or without modification, are
permitted in any medium without royalty provided the copyright notice and this
notice are preserved.  This file is offered as-is, without any warranty.
-->

The Protractor Gesture Recognition Algorithm in Scala
=====================================================

In December while working on my Sudoku app I thought it would be nice to
recognize numbers scribbled on a touch-screen. In my search for a library to
handle this I stumbled across the **$1** algorithm and thought I would test
their claim of being easy to implement. In doing that I became more and more
interested, and that is what started this project.

Details on the algorithm itself will be added to the scaladoc comments.

The library lives in the `protractor` and `protractor.minutiae` packages.
Given the small size of the algorithm it is difficult to justify hiding
implementation details, so everything is public. But typical applications
should only need to use the API defined in the top-level package. 

This project also contains unit tests for the library, and a bare-bones GUI
for interactive testing.


Dependencies
------------

The library has no dependencies, but the unit tests do. I am using version
0.11.2 of SBT to manage them. You will need to run `sbt update` as the first
step of any build (of course, this is automatic if you build with SBT).


Building with SBT
-----------------

As expected the `test` task runs the unit tests. To run the interactive test
gui use `test:run`.


Building with Eclipse
---------------------

I am using the 2.9.x flavor of version 2.0.0 of Scala-IDE. I am also going
against recommendations and using Eclipse 3.7 (Indigo) and JDK 7u2. Here's
how to add the three Protractor projects to your workspace:

 * menu `File / Import...`
 * source `General / Existing Projects into Workspace`
 * option `Select root directory` browse to the git working directory
 * the three projects should be listed and all be selected
 * do **NOT** copy them into workspace - you will break links
 * just click `Finish`

The projects include two run/debug configurations, one for the unit tests,
and one for the interactive GUI. Use the `Run / Run Configurations...` (or
`Run / Debug Configurations...`) menu to launch them.

_Warning_ : The TestGUI and UnitTests source code links overlap, so be
careful about opening files from the correct project. Sigh... perhaps some
day resource filters will work with linked resources.


Release Notes
-------------

In spite of being publically available on github, I don't consider this
project as officially having been released yet. But feel free to poke around
and poke fun if you want. There's a lot of work to do, here are my ideas for
milestones for future versions:

 * `0.4` clean up the source code
	* move old cruft to the test packages
	* write a bunch of comments (scaladoc & otherwise)
 * `0.5` add the 3D version of the algorithm
 * `0.6` add tools to evaluate performance & results
	* somehow to collect a bunch of data
	* goal is to justify deviations from the published algorithm
 * `Phase 2` **?**
 * `Phase 3` Profit
