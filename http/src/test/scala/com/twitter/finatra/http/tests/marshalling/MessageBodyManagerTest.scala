package com.twitter.finatra.http.tests.marshalling

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.internal.marshalling.MessageBodyManager
import com.twitter.finatra.http.marshalling.{MessageBodyComponent, MessageBodyReader}
import com.twitter.finatra.http.modules.{MessageBodyModule, MustacheModule}
import com.twitter.finatra.json.modules.FinatraJacksonModule
import com.twitter.inject.Test
import com.twitter.inject.app.TestInjector
import org.specs2.mock.Mockito

class MessageBodyManagerTest extends Test with Mockito {

  val request = mock[Request]
  val injector = TestInjector(MessageBodyModule, FinatraJacksonModule, MustacheModule)

  val messageBodyManager = injector.instance[MessageBodyManager]
  messageBodyManager.add[DogMessageBodyReader]()
  messageBodyManager.add(new FatherMessageBodyReader)

  "parse car" in {
    messageBodyManager.add[Car2MessageBodyReader]()
  }

  "parse dog" in {
    messageBodyManager.read[Dog](request) should equal(Dog("Dog"))
  }

  "parse father" in {
    messageBodyManager.read[Son](request) should equal(Son("Nykolas"))
  }
}

case class Car2(name: String)

case class Dog(name: String)

trait Father

case class Son(name: String) extends Father

class Car2MessageBodyReader extends MessageBodyReader[Car2] {
  def parse(request: Request): Car2 = Car2("Car")
}

class DogMessageBodyReader extends MessageBodyReader[Dog] {
  def parse(request: Request): Dog = Dog("Dog")
}

class FatherMessageBodyReader extends MessageBodyReader[Father] {
  implicit def ev: Manifest[Father] = manifest[Father]

  override def parse(request: Request): Father = {
    if(ev.runtimeClass == classOf[Son]) Son("Nykolas").asInstanceOf[Father] else throw new RuntimeException("test should fail if got here")
  }
}
