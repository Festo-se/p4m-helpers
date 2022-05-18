# Papyrus4Manufacturing helpers

Library of helper code used by the [Papyrus4Manufacturing](https://www.eclipse.org/papyrus/components/manufacturing) AAS code generator. Papyrus4Manufacturing is a visual development tool for asset administration shells. It allows modeling AASs and then generating executable Java code for them. The generated code has two dependencies:

- Eclipse BaSyx SDK
- This library

Though it is possible to use this package in any context when working with the [Eclipse BaSyx SDK](https://github.com/eclipse-basyx/basyx-java-sdk), it's declared goal is to facilitate the generation of simpler and more efficient code from Papyrus4Manufacturing.

## Getting started

Assuming you _do_ want to use this library in your own project, put this dependency into your `pom.xml`:

```xml
<dependency>
  <groupId>com.festo.aas</groupId>
  <artifactId>p4m-helpers</artifactId>
  <version>1.0.0</version>
</dependency>
```

Then code away!

## Documentation

Find the javadoc at [javadoc.io](https://javadoc.io/doc/com.festo.aas/p4m-helpers).

## Contributing

Any contributions in form of bug repors, feature requests or even (_gasp_) pull requests are very welcome.

To create a pull request:

1. Fork this repository.
2. Commit and push your changes to your `main` or feature branch.
3. Open a PR.

## License

This code is made available under the Eclipse Public License v2.0 (EPL-2.0). See `LICENSE.txt` for more information.

## Contact

Moritz Marseu - Festo Didactic SE - moritz.marseu@festo.com

## Acknowledgedments

This library was developed in the [CanvAAS](https://www.eitmanufacturing.eu/what-we-do/eit-manufacturing-case-studies/case-study-canvaas-bridging-a-fragmented-industry-landscape) activity, co-funded by the [EIT Manufacturing](https://www.eitmanufacturing.eu/).

![EIT Manufacturing Co-funding logo](doc/EITM_EU_CoFund_RGB_Landscape_Full%20colour.png)
