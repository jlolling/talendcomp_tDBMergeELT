# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Talend component tDBMergeELT: builds and runs a SQL MERGE from a source query into a target table, using the target's metadata.
User-facing behaviour and parameters are described in `README.md` and the manuals in `doc/`.

## Build

```bash
mvn clean package                      # test, assemble component(s), copy them to ${custom_component_folder}
mvn test -Dtest=SomeTest#someMethod   # single test class or method
```

Java 8; `cimt-talendcomp-maven-plugin` 3.1. `custom_component_folder` is defined in `~/.m2/settings.xml`.

## Structure

- `src/main/java` (`de.jlo.datamodel`, `de.jlo.datamodel.ext`, `de.jlo.talendcomp.dbmerge`): the runtime library. Generated job code calls these classes by fully qualified name.
- `src/main/components/<Name>/`: `<Name>_java.xml` (descriptor), `*.javajet` (code-generation templates), `<Name>_messages.properties` (labels, `LONG_NAME` required) and the icon. A parameter change usually touches all three plus the library.
- Generated at `package`, do not hand-edit: the `<IMPORTS>` block and HEADER `VERSION`/`RELEASE_DATE` in `_java.xml`, and jars in the component folders. Bump the version in `pom.xml`.
- `de.jlo.datamodel` is a copy, not a shared artifact. Diverged copies exist in talendcomp_tDBMergeELT, talendcomp_tDBTableTransfer and talendcomp_tOracleMergeELT; a fix there may be needed in the siblings too.
