FxCop inspections support for TeamCity

Installation
============

FxCop support bundled with TeamCity 4 and higher.

Current sources provided as reference for plugin writers.


Development notes
=================

1) Use 'deploy' artifact to build plugin and deploy it to local server instance
2) Use 'IntelliJ IDEA Project' runner to build plugin inside of TeamCity
	- Set 'Path to the project' - fxcop.ipr
	- Set 'Artifacts to build' - plugin-zip (to build correctly packed plugin)
	- Set 'Run configurations to execute' - Run Tests (to run all plugin tests)
