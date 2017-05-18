package connectors

import com.google.inject.Inject
import play.api.Logger
import play.api.libs.json._
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

/**
  * Created by Overlord59 on 18/05/2017.
  */
class MongoConnector @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends ReactiveMongoComponents {

  private def collection(collectionName: String): Future[JSONCollection] = reactiveMongoApi.database.map { database => database.collection(collectionName) }

  def getAllEntries[T](collectionName: String)(implicit reads: Reads[T]): Future[List[T]] = {
    val getCollection = collection(collectionName)
    val handler: Cursor.ErrorHandler[List[T]] = {
      (last: List[T], error: Throwable) =>
        Logger.warn(s"Error encountered fetching entry: ${error.getMessage}")

        if (last.isEmpty) {
          Cursor.Cont(last)
        } else Cursor.Fail(error)
    }

    def filterList(collection: JSONCollection): Future[List[T]] = {
      collection.find(Json.obj()).cursor[T]().collect[List](maxDocs = -1, err = handler)
    }

    for {
      collection <- getCollection
      list <- filterList(collection)
    } yield list
  }

  def getEntry[T](collectionName: String, key: String, value: JsValue)(implicit reads: Reads[T]): Future[Option[T]] = {
    val getCollection = collection(collectionName)

    def filterList(collection: JSONCollection): Future[Option[T]] = {
      collection.find(JsObject(Map(key -> value))).one[T]
    }

    for {
      collection <- getCollection
      entry <- filterList(collection)
    } yield entry
  }

  def putEntry[T](collectionName: String, document: T)(implicit writes: OWrites[T]): Future[Unit] = {

    val getCollection = collection(collectionName)

    def insertDocument(collection: JSONCollection): Future[WriteResult] = {
      collection.insert(document)
    }

    def mapResult(result: WriteResult): Future[Unit] = {
      if (result.ok) Future.successful {}
      else throw new Exception(s"Failed to write to database: ${result.code.get} ${result.writeErrors}")
    }

    for {
      collection <- getCollection
      insert <- insertDocument(collection)
      result <- mapResult(insert)
    } yield result
  }

  def updateEntry[T](collectionName: String, key: String, value: JsValue, document: T)(implicit oFormat: OFormat[T]): Future[Unit] = {
    val getCollection = collection(collectionName)

    def updateDocument(collection: JSONCollection): Future[UpdateWriteResult] = {
      collection.update(Json.obj(key -> value), document)
    }

    def mapResult(result: UpdateWriteResult): Future[Unit] = {
      if (result.ok) Future.successful {}
      else throw new Exception(s"Failed to update document in database: ${result.code.get} ${result.errmsg.get}")
    }

    for {
      collection <- getCollection
      insert <- updateDocument(collection)
      result <- mapResult(insert)
    } yield result
  }
}
