# Guidance on coding patterns

## EditorConfig

To ensure a common file format, there is a `.editorConfig` file [in place](../.editorconfig). To respect this file, [check](http://editorconfig.org/#download) if your editor does support it natively or you need to download a plugin.

### Commit Message Style

Write [meaningful commit messages](http://who-t.blogspot.de/2009/12/on-commit-messages.html) and [adhere to standard formatting](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html).

Good commit messages speed up the review process and help to keep this project maintainable in the long term.

## Code Style

Generally, the code should follow any stylistic and architectural guidelines prescribed by the project. In the absence of guidelines, mimic the styles and patterns in the existing code-base.

The intention of this section is to describe the code style for this project. As reference document, the [Groovy's style guide](http://groovy-lang.org/style-guide.html) was taken. For further reading about Groovy's syntax and examples, please refer to this guide.

This project is intended to run in Jenkins [[2]](https://jenkins.io/doc/book/getting-started/) as part of a Jenkins Pipeline [[3]](https://jenkins.io/doc/book/pipeline/). It is composed by Jenkins Pipeline's syntax, Groovy's syntax and Java's syntax.

Some Groovy syntax is not yet supported by Jenkins. It is also the intention of this section to remark which Groovy syntax is not yet supported by Jenkins.

As Groovy supports 99% of Java’s syntax [[1]](http://groovy-lang.org/style-guide.html), many Java developers tend to write Groovy code using Java's syntax. Such a developer should also consider the following code style for this project.

### General remarks

Variables, methods, types and so on shall have meaningful, self-describing names. Doing so makes understanding code easier and requires less commenting. It helps people who did not write the code to understand it better.

Code shall contain comments to explain the intention of the code when it is unclear what the intention of the author was. In such cases, comments should describe the "why" and not the "what" (that is in the code already).

### Omit semicolons

### Use the return keyword

In Groovy it is optional to use the _return_ keyword. Use explicitly the _return_ keyword for better readability.

### Use def

When using _def_ in Groovy, the type is Object. Using _def_ simplifies the code, for example imports are not needed, and therefore the development is faster.

### Do not use a visibility modifier for public classes and methods

By default, classes and methods are public, the use of the public modifier is not needed.

### Do not omit parentheses for Groovy methods

In Groovy is possible to omit parentheses for top-level expressions, but [Jenkins Pipeline's syntax](https://jenkins.io/doc/book/pipeline/syntax/) use a block, specifically `pipeline { }` as top-level expression [[4]](https://jenkins.io/doc/book/pipeline/syntax/). Do not omit parenthesis for Groovy methods because Jenkins will interpret the method as a Pipeline Step. Conversely, do omit parenthesis for Jenkins Pipeline Steps.

### Omit getters and setters

When declaring a field without modifier inside a Groovy bean, the Groovy compiler generates a private field and a getter and setter.

### Do not use _with()_ operator

The _with_ operator is not yet supported by Jenkins, and it must not be used or encapsulated in a @NonCPS method.

### Use _==_ operator

Use Groovy’s `==` instead of Java `equals()` to avoid NullPointerExceptions. To compare the references of objects, instead of `==`, you should use `a.is(b)` [[1]](http://groovy-lang.org/style-guide.html).

### Use GStrings

In Groovy, single quotes create Java Strings, and double quotes can create Java Strings or GStrings, depending if there is or not interpolation of variables [[1]](http://groovy-lang.org/style-guide.html). Using GStrings variable and string concatenation is more simple.

## Use 'single quotes' for Strings and constants

## Use "double quotes" for GStrings

## Use '''triple single quotes''' for multiline Strings

## Use """triple double quotes""" for multiline GStrings

## Use /slash/ for regular expressions

This notation avoids to double escape backslashes, making easier working with regex.

### Use native syntax for data structures

Use the native syntax for data structures provided by Groovy like lists, maps, regex, or ranges of values.

### Use additional Groovy methods

Use the additional methods provided by Groovy to manipulate String, Files, Streams, Collections, and other classes.
For a complete description of all available methods, please read the GDK API [[5]](http://groovy-lang.org/groovy-dev-kit.html).

### Use Groovy's switch

Groovy’s switch accepts any kind of type, thereby is more powerful. In this case, the use of _def_ instead of a type is necessary.

### Use alias for import

In Groovy, it is possible to assign an alias to imported packages. Use alias for imported packages to avoid the use of fully-qualified names and increase readability.

### Use Groovy syntax to check objects

In Groovy a null, void, equal to zero, or empty object evaluates to false, and if not, evaluates to true. Instead of writing null and size checks e.g. `if (name != null && name.length > 0) {}`, use just the object `if (name) {}`.

### Use _?._ operator

Use the safe dereference operator  _?._, to simplify the code for accessing objects and object members safely. Using this operator, the Groovy compiler checks null objects and null object members, and returns _null_ if the object or the object member is null and never throws a NullPointerException.

### Use _?:_ operator

Use Elvis operator _?:_ to simplify default value validations.

### Use _any_ keyword

If the type of the exception thrown inside a try block is not important, catch any exception using the _any_ keyword.

### Use _assert_

To check parameters, return values, and more, use the assert statement.

### Do not use abstract classes and traits

Ensure neither abstract classes nor traits are used in the shared library.

### Do not use JsonSlurper

Use `JsonSlurperClassic` instead of the non-serializable JsonSlurper class.

## References

[1] Groovy's syntax: [http://groovy-lang.org/style-guide.html](http://groovy-lang.org/style-guide.html)

[2] Jenkins: [https://jenkins.io/doc/book/getting-started/](https://jenkins.io/doc/book/getting-started/)

[3] Jenkins Pipeline: [https://jenkins.io/doc/book/pipeline/](https://jenkins.io/doc/book/pipeline/)

[4] Jenkins Pipeline's syntax: [https://jenkins.io/doc/book/pipeline/syntax/](https://jenkins.io/doc/book/pipeline/syntax/)

[5] GDK: Groovy Development Kit: [http://groovy-lang.org/groovy-dev-kit.html](http://groovy-lang.org/groovy-dev-kit.html)
