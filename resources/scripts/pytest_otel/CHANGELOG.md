# version 1.0.2

commit cdbea9e1ec3a21fe60f44460c62e83189f6acec3
Author: Ivan Fernandez Calvo <kuisathaverat@users.noreply.github.com>
Date:   Thu Mar 24 12:18:28 2022 +0100

    fix: show traces only on debug mode

commit bc2f73754c2786fc1bae6e922dccb9615789cb2b
Author: Ivan Fernandez Calvo <kuisathaverat@users.noreply.github.com>
Date:   Fri Feb 4 12:29:29 2022 +0100

    fix: use different tests for the demos (#1523)

# Version 1.0.1

commit ea2eb6447145db7d31c73e6811253b5b6cdba965
Author: Ivan Fernandez Calvo <kuisathaverat@users.noreply.github.com>
Date:   Mon Jan 31 18:41:17 2022 +0100

    fix: Pytest update attr (#1521)

    * feat: update the attribute references

    * chore: bump version

    * fix: remove un used import

    * chore: bump version


# Version 1.0.0

commit f59ee3aa8e020e6cbadfaa9255398eacb046004d
Author: Ivan Fernandez Calvo <kuisathaverat@users.noreply.github.com>
Date:   Mon Jan 31 11:31:40 2022 +0100

    feat: update the attribute references (#1496)

    * feat: update the attribute references

    * chore: bump version

commit 5b1874343b6f5f6ca7f2c49997e878bb20a9a00d
Author: Victor Martinez <v1v@users.noreply.github.com>
Date:   Mon Dec 20 14:42:38 2021 +0000

    Update main (#1468)

commit fc0987200533e32ea2cf119e29ec69e31db526ba
Author: Ivan Fernandez Calvo <kuisathaverat@users.noreply.github.com>
Date:   Mon Dec 20 12:45:54 2021 +0100

    fix: Otel avoid conflicts (#1464)

    * fix: avoid settings conflict

    * fix: not enable debug by default

    * chore: bump version

# Version 0.0.6

commit 234b7e26e6fd83263eeb49ca5c83f6c9ac4051f6
Author: Ivan Fernandez Calvo <kuisathaverat@users.noreply.github.com>
Date:   Thu Dec 16 13:37:09 2021 +0100

    fix: improve attributes access (#1462)

    * fix: improve attributes access

    * Update resources/scripts/pytest_otel/src/pytest_otel/__init__.py

    Co-authored-by: Mike Place <cachedout@users.noreply.github.com>>

    * Update resources/scripts/pytest_otel/setup.cfg

    Co-authored-by: Mike Place <cachedout@users.noreply.github.com>>

# Version 0.0.4

commit 11972c8aa03ee83b101ab59984b2271ec99667c0
Author: Ivan Fernandez Calvo <kuisathaverat@users.noreply.github.com>
Date:   Tue Dec 14 14:25:53 2021 +0100

    fix: fix pytest_otel authentication (#1452)

    * fix_ downgrade Python Otel to 1.2

    * test: update demo to use authentication

    * chore: bump version

    * fix: use Otel 1.5

    * Update resources/scripts/pytest_otel/docs/demos/elastic/demo.env

# Version 0.0.3

commit 17db7621b93029326e36a183c71b4617a279e442
Author: Ivan Fernandez Calvo <kuisathaverat@users.noreply.github.com>
Date:   Mon Dec 13 15:12:41 2021 +0100

    feat: add demos (#1441)

    * feat: add demos

    * chore: add credits and license

    * fix: full URL for the demos in the main README

# Version 0.0.3

commit e9096dfb21a696878da1730777ca33f0389a53d6
Author: Ivan Fernandez Calvo <kuisathaverat@users.noreply.github.com>
Date:   Fri Dec 3 13:39:50 2021 +0100

    feat: publish pytest_otel (#1392)

    * feat: publish pytest_otel

    * chore: refactor

    * feat: tests

    * feat: configure file exporter

    * chore: use pytester instead testdir

    * chore: refactor

    * chore: update ignore

    * fix: license

    * docs: update README

    * feat: update otel collector run

    * feat: update clean target

    * feat: release target

    * feta: docker-compose for testing

    * chore: bump version

    * fix: typo

    * fix: remove email

    * fix: update license headers

    * docs: documents tests

    * runs tests in a subprocess

    * remove dependencies

    * feat: it tests

    * docs: add functions comments

    * fix: report correct test suit status

    * fix: avoid index out of range errors

    * feat: Integration tests

    * feat: remove junit files in the clean target

    * chore: remove release target

# Version 0.0.2

commit 9c1f57a5492caf6f0212eda6af000d0971c72205
Author: Ivan Fernandez Calvo <kuisathaverat@users.noreply.github.com>
Date:   Tue Jul 27 22:18:35 2021 +0200

    feat: Otel pytest plugin (#1217)

    * feat: Otel pytest plugin

    * Apply suggestions from code review

    Co-authored-by: Cyrille Le Clerc <cyrille-leclerc@users.noreply.github.com>>

    * Apply suggestions from code review

    * Apply suggestions from code review

    Co-authored-by: cachedout <cachedout@users.noreply.github.com>>

    * Update resources/scripts/pytest_otel/pytest_otel.py

    * fix: insecure to the value of insecure var

    * fix: allow more environment variables when we run the tests

    * docs: headers should be lowercase

    * fix: set proper context

    * fix: distributed traccing

    * Apply suggestions from code review

    Co-authored-by: Victor Martinez <v1v@users.noreply.github.com>

    Co-authored-by: Cyrille Le Clerc <cyrille-leclerc@users.noreply.github.com>>
    Co-authored-by: cachedout <cachedout@users.noreply.github.com>>
    Co-authored-by: Victor Martinez <v1v@users.noreply.github.com>
