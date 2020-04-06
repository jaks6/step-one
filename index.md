---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults

layout: default
---

Welcome to the documentation and tutorials page of STEP-ONE!

## Tutorials

  <ul>
  	{% assign sorted = site.categories["tutorial"] | sort: 'date' | reverse  %}
    {% for post in sorted %}
      <li><a href="{{ site.baseurl }}{{ post.url }}">{{ post.title }}</a></li>
    {% endfor %}
  </ul>
 
## Configuring STEP-ONE
<ul>

	<li><a href="{{ site.baseurl }}{% link _posts/2020-04-06-configuration.md %}">Configuring STEP-ONE simulations</a></li>
</ul>

Step-ONE is based on the ONE simulator, the Flowable process engine and Flowable's Process modeler. These tutorials do not exhaustively cover all functionality available in STEP-ONE, as they have been inherited from the before-mentioned tools. Thus it is highly advisable to also study their documentation to leverage the full capabilities of STEP-ONE. The relevant materials can be found at:

 [ https://flowable.com/open-source/docs/ ]
 [ http://akeranen.github.io/the-one/ ]

## Advanced examples
