# *A collection of `SuperCollider` classes*
## Overview
This package collects my `SuperCollider` classes and extensions. These are:

* `CScore`
  A subclass of `Score` oriented towards real time playing. Not thoroughly tested.

* `Extrema`
    Given an `Array` instance with the values of a function over an interval, this class computes the positions and values of local extrema. It's methods return `Array` instances with those values.

* `Girgensohn`
    A singleton `Class` that can be used to compute and collect the values of non differentiable functions over the interval `[0, 1]`.

* `MultiPortNetAddr`
	`OSC` communication with applications that share the same `IP` address but listen on different ports.

* `PetriNets`
	A collection of classes that build a *Petri Net* framework for modeling sound events and structures. For an introduction and examples please refer to [about-PetriNets.scd](PetriNets/about-PetriNets.scd).

* `PGraphWalk`
   A subclass of `Pattern`. Generates a random walk over the vertices of a given graph.

* `ScoreTimer`
  A GUI that shows the current playback time of a given `Score` object that is played in real time.

* `Array.pisano`
  A class method that extends `Array`. Let `n` be a positive integer. `pisano` takes `n` as argument and
  returns an `Array` instance with the *pisano* period of `n`. That is, it collects the period of the
  sequence `F_k mod(n)` where `F_k` is the `k-ieth` Fibonacci number.

## Requirements
`SuperCollider` version 2.7.2 or greater.

## Installation
Clone repository under directory `Platform.systemExtensionDir`.

## Contribute
- Use the classes and report issues, errors and bugs.
- Help improve the code.

## License
[GPLv3](LICENSE)
