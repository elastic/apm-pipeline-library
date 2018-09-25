# apm-pipeline-library

Jenkins pipeline shared library for the project APM

```
(root)
+- src                     # Groovy source files
|   +- co
|       +- elastic
|           +- Bar.groovy  # for org.foo.Bar class
+- vars
|   +- foo.groovy          # for global 'foo' variable
|   +- foo.txt             # help for 'foo' variable
+- resources               # resource files (external libraries only)
|   +- co
|       +- elastic
|           +- bar.json    # static helper data for org.foo.Bar
```

* [Pipeline](https://jenkins.io/doc/book/pipeline/)
* [Pipeline shared library](https://jenkins.io/doc/book/pipeline/shared-libraries/)
