---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults

layout: default
---

### Welcome to the documentation and tutorials page of STEP-ONE!

The below tutorials walk you through how to create process-oriented scenarios with STEP-ONE. Because STEP-ONE is based on the *[ONE simulator](http://akeranen.github.io/the-one/)* and *Flowable*, it is advisable to also refer to their documentation. The relevant materials can be found at:

* [https://flowable.com/open-source/docs/]( https://flowable.com/open-source/docs/ ) 
* [http://akeranen.github.io/the-one/](http://akeranen.github.io/the-one/ ) 

## Running & Configuring STEP-ONE simulations

* STEP-ONE scenarios can be run with Gradle, providing as argument the settings file to use:

```
./gradlew run --args samples/tutorial1_basic_messaging/settings.txt
```

* STEP-ONE processes can be created using the STEP-ONE version of Flowable BPMN modeller:
  * Install [Docker](https://docs.docker.com/engine/install/) and [Docker Compose](https://docs.docker.com/compose/install/)
  * Go to *step-one-modeler* directory, which contains the *docker-compose.yml* file.  `cd ./step-one/step-one-modeler`
  * Run `docker-compose up`
  * After the web-app service has finished initialising, use a browser to open the modeller at:
    http://localhost:8888/flowable-modeler

## Tutorials

*The following implemented versions of these tutorials can be found [here](https://github.com/jaks6/step-one/tree/master/step-one-main/samples)*. That includes settings files and BPMN files.

  <ul>
  	{% assign sorted = site.categories["tutorial"] | sort: 'date'  %}
    {% for post in sorted %}
      <li><a href="{{ site.baseurl }}{{ post.url }}">{{ post.title }}</a></li>
    {% endfor %}
  </ul>





## Advanced examples
