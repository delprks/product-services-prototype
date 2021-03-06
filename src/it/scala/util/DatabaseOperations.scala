package util

import java.sql.Timestamp

import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random
import slick.driver.H2Driver.api._

trait DatabaseOperations {

  val database: Database

  protected def createOffersTable(): Unit = {
    Await.result(database.run(
      sqlu"""
        CREATE TABLE public.offer
        (
            id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
            user_id INT NOT NULL,
            title VARCHAR(255) NOT NULL,
            description VARCHAR(755) NOT NULL,
            headline VARCHAR(40),
            condition VARCHAR(40) NOT NULL,
            available_from TIMESTAMP NOT NULL,
            available_to TIMESTAMP NOT NULL,
            starting_price INT NOT NULL,
            currency VARCHAR(40) NOT NULL,
            category VARCHAR(255) NOT NULL,
            status VARCHAR(20)
        )
      """), Duration.Inf)
  }

  protected def dropOffersTable() = {
    Await.result(database.run(sqlu"""DROP TABLE IF EXISTS public.offer"""), Duration.Inf)
  }

  protected def insertOffer(
    id: Int = randomId(),
    userId: Int = randomId(),
    title: String = randomString(10),
    availableFrom: Timestamp = SQLPastDate,
    availableTo: Timestamp = SQLFutureDate,
    category: String = randomString(10),
    status: String = "available"
  ): Unit = {
    Await.result(database.run(
      sqlu"""
        INSERT INTO public.offer
        VALUES (
          $id,
          $userId,
          $title,
          ${randomString()},
          ${randomString()},
          ${randomString()},
          $availableFrom,
          $availableTo,
          ${randomInteger()},
          ${randomString()},
          $category,
          $status
        );
      """), Duration.Inf)
  }

  private val randomIntegerStream: Stream[Int] = Stream.continually(Random.nextInt(Integer.MAX_VALUE))

  private val distinctIdIterator = randomIntegerStream.distinct.iterator

  private def randomId(): Int = distinctIdIterator.next()

  private def randomInteger(min: Int = 0, max: Int = 100): Int = Random.nextInt(max - min + 1) + min

  private def randomString(length: Int = 4): String = Random.alphanumeric.take(length).mkString

  private def randomDateTime(): DateTime = new DateTime().minusDays(randomInteger(0, 365))

  private def randomSqlTimestamp(): Timestamp = new Timestamp(randomDateTime().getMillis)

  val SQLPastDate: Timestamp = {
    val dateTime = new DateTime("2016-06-17T14:20:25").getMillis
    new Timestamp(dateTime)
  }

  val SQLFutureDate: Timestamp = {
    val dateTime = new DateTime("2025-06-17T14:20:25").getMillis
    new Timestamp(dateTime)
  }

  val SQLYesterdayDate: Timestamp = {
    val dateTime = DateTime.now().minusDays(1).getMillis
    new Timestamp(dateTime)
  }

}
