---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults

layout: default
---

### Welcome to the documentation and tutorials page of STEP-ONE!

The below tutorials walk you through how to create process-oriented scenarios with STEP-ONE. Because STEP-ONE is based on the *[ONE simulator](http://akeranen.github.io/the-one/)* and *Flowable*, it is advisable to also refer to their documentation. The relevant materials can be found at:

* [https://flowable.com/open-source/docs/]( https://flowable.com/open-source/docs/ ) 
* [http://akeranen.github.io/the-one/](http://akeranen.github.io/the-one/ ) 

## Configuring STEP-ONE simulations
<ul>
	<li><a href="{{ site.baseurl }}{% link _posts/2020-04-06-configuration.md %}">Configuring 		STEP-ONE simulations</a></li>
</ul>
## Tutorials

  <ul>
  	{% assign sorted = site.categories["tutorial"] | sort: 'date'  %}
    {% for post in sorted %}
      <li><a href="{{ site.baseurl }}{{ post.url }}">{{ post.title }}</a></li>
    {% endfor %}
  </ul>





## Advanced examples
