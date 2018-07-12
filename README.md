# Foodtruck locator for Slack!

This repository contains instructions on how to add a custom Slack integration that queries a website to list foodtrucks in your area! I like to eat at food trucks when I'm at work, and sometimes it's good to have an idea of what's around before walking around the block. So I created a Scala app using the Play framework which I deploy to Heroku, and added integration to Slack, so the user can enter `/foodtrucks` and get a listing of what's nearby.

# Setting up a Heroku app

To get started running the foodtruck locator on your own, first clone the respository locally. My instructions involve using Heroku to deploy the Play application, but feel free to use any cloud platform that suits your liking. If you decide to use Heroku, first follow the instructions for setting up the [Heroku Toolbelt](https://devcenter.heroku.com/articles/getting-started-with-scala#set-up). Then pull down the slack-foodtrucks repository and create a Heroku app:

```
$ git clone https://github.com/andy327/slack-foodtrucks.git
$ cd slack-foodtrucks/
$ heroku create
Creating app... done, stack is cedar-14
https://pure-waters-2527.herokuapp.com/ | https://git.heroku.com/pure-waters-2527.git
```

If you'd like to set the defaults for latitude and longitude for your foodtrucks query, edit the values at the bottom of the `conf/application.conf` file:

```
# Coordinates for food trucks:
foodtrucks {
  latitude =  40.727
  longitude = -74.006
}
```

To test out the REST call locally, you can build the the project in sbt and deploy a local Heroku instance:


```
$ sbt compile stage
$ heroku local web
```

Test out the query by accessing `localhost:5000/foodtrucks` in your browser. If everything worked correctly, you should see a listing of nearby foodtrucks. You can then deploy your app onto Heroku's platform and try it out:

```
$ git push heroku master
Counting objects: 23, done.
Delta compression using up to 4 threads.
Compressing objects: 100% (21/21), done.
Writing objects: 100% (23/23), 4.77 KiB | 0 bytes/s, done.
Total 23 (delta 0), reused 0 (delta 0)
remote: Compressing source files... done.
remote: Building source:
remote:
remote: -----> Play 2.x - Scala app detected
remote: -----> Installing OpenJDK 1.7... done
remote: -----> Priming Ivy cache (Scala-2.11, Play-2.3)...
remote: -----> Running: sbt compile stage
...
$ heroku open foodtrucks
Opening pure-waters-2527... done
$ Created new window in existing browser session.
```

# Modifying the Play application

The slack-footrucks application makes a cURL request to the website nyctruckfood.com, but you can change this by modifying the `app/controllers/Truck.scala` file. You can also change how you handle the request and output the locations:

```
case class Truck (
  name: String,
  address: String,
  cuisine: List[String],
  url: String) {
  override def toString: String = s"""$name (${cuisine.mkString(",")}) @ $address [$url]"""
}

object Truck {
  ...

  def apply(jsonNode: JsonNode): Truck = {
    val info = jsonNode.get("obj")

    Truck(
      info.get("name").asText,
      info.get("last_seen").get("address").asText,
      info.get("cuisine").elements.asScala.map(_.asText).toList,
      info.get("url").asText
    )
  }

  def listAll(lat: Double = defaultLat, lon: Double = defaultLon): List[Truck] = {
    val result = Seq("curl", "-XGET", s"http://nyctruckfood.com/api/trucks/search?q=$lat,$lon").!!
    mapper.readTree(result).elements.asScala.map(Truck(_)).toList
  }
}
```

`app/controllers/Application.scala` is responsible for defining the action that will get mapped to the /foodtrucks route. This requires adding an entry to `conf/routes` as such:
```
GET     /foodtrucks                 controllers.Application.foodtrucks
```

# Adding a custom Slack integration

Once you're done modifying the actions and endpoint for your foodtrucks application, and have tested that the request works, adding a custom Slack integration is straight-forward. First visit the Slack [configurations page](https://slack.com/services/new/slash-commands) and click the 'Add Configuration' button. Then go ahead and fill out the settings you want, making sure to link the URL to a GET request for your app on Heroku:

![Slack Integration](https://i.imgur.com/boo0muA.png)

When you're ready, click 'Save Integration' and go ahead and test out your new Slack command! Try going to your Slackbot and typing the command `/foodtrucks`:

![/foodtrucks](https://i.imgur.com/DeirKHo.png)
