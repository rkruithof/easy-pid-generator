/**
 * Copyright (C) 2015 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.pid.generator

import nl.knaw.dans.easy.pid._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.util.{ Failure, Success }

class URNGeneratorSpec extends SeedDatabaseFixture
  with ConfigurationSupportFixture
  with URNGeneratorWiring
  with ConfigurationComponent
  with SeedStorageComponent
  with DatabaseComponent
  with PidFormatterComponent
  with DebugEnhancedLogging {

  override val database: Database = new Database {}

  "namespace" should "have the correct value based on the properties" in {
    urnGenerator.formatter.namespace shouldBe "urn:nbn:nl:ui:13-"
  }

  "dashPosition" should "have the correct value based on the properties" in {
    urnGenerator.formatter.dashPosition shouldBe 4
  }

  "firstSeed" should "have the correct value based on the properties" in {
    urnGenerator.seedStorage.firstSeed shouldBe 1L
  }

  "next" should "return the initial URN when it is never called before and store this URN in the database" in {
    val urn = urnGenerator.next()
    urn shouldBe Success("urn:nbn:nl:ui:13-0000-01")

    inside(database.getSeed(URN)) {
      case Success(Some(seed)) =>
        seed shouldBe 1L
        urnGenerator.formatter.format(seed) shouldBe urn.get
    }
  }

  it should "return the second URN when it is called for the second PID and store this URN in the database" in {
    urnGenerator.next() shouldBe a[Success[_]]
    val urn = urnGenerator.next()
    urn shouldBe Success("urn:nbn:nl:ui:13-001h-aq")

    inside(database.getSeed(URN)) {
      case Success(Some(seed)) =>
        seed shouldBe 69074L
        urnGenerator.formatter.format(seed) shouldBe urn.get
    }
  }

  it should "fail when the seed in the database is the last seed available and leave the database unchanged" in {
    database.initSeed(URN, 1752523756L) shouldBe a[Success[_]]
    urnGenerator.next() should matchPattern { case Failure(RanOutOfSeeds(URN)) => }

    database.getSeed(URN) shouldBe Success(Some(1752523756L))
  }
}
