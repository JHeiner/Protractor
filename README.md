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
their claim of being easy to implement. In doing that I became interested in
the details, and that is what started this project.

Details on the algorithm itself will be added to the scaladoc comments.

The library lives in the `protractor` and `protractor.minutiae` packages.
Given the small size of the algorithm it is difficult to justify hiding
implementation details, so everything is public. But typical applications
would only need to use the API defined in the top-level package. 

This project also contains unit tests for the library, and a bare-bones GUI
for interactive testing.


Building
--------

The library has no dependencies, but the unit tests do. I am using version
0.11.2 of SBT to manage them. There are 2 sub-projects:

 * `lib` contains the library and the unit tests (task `test`)
 * `gui` contains the interactive testing GUI (task `run`)

The SBT configuration is a little non-standard to avoid having a huge deep
directory structure for five source code files.


Release Notes
-------------

In spite of being publically available on github, I don't consider this
project as officially having been released yet. But feel free to poke around
and poke fun if you want. There's a lot of work to do, here are my ideas for
milestones for future versions:

 * `0.3` support building using the Eclipse Scala-IDE
 * `0.4` clean up the source code
          * move old cruft to the test packages
          * write a bunch of comments (scaladoc & otherwise)
 * `0.5` add the 3D version of the algorithm
 * `0.6` add tools to evaluate performance & results
          * somehow to collect a bunch of data
          * goal is to justify deviations from the published algorithm
 * `Phase 2` **?**
 * `Phase 3` Profit
