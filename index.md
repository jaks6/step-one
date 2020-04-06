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
      <li><a href="{{ post.url | prepend: site.baseurl }}">{{ post.title }}</a></li>
    {% endfor %}
  </ul>
 
## Configuring STEP-ONE
<ul>

	<li><a href="{{ site.baseurl }}{% link _posts/2020-04-06-configuration.md %}">Configuring STEP-ONE simulations</a></li>
</ul>

## Advanced examples
