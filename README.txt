How to update fxcop.xml
-----------------------

1) IDEA->Build->Generate Ant Build:
   - "Generate single-file and build"
   - "Overwrite previously generated files"
   - Uncheck "Enable UI forms compilation" and "Use JDSK definitions from project files"
2) remove <property file="fxcop.properties"/> line from fxcop.xml
