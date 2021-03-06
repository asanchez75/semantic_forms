package deductions.runtime.abstract_syntax

import deductions.runtime.core.FormModule
import deductions.runtime.semlogs.TimeSeries
import org.w3.banana.RDF

import scala.collection.mutable
import scala.language.{existentials, postfixOps}

/** TODO move to package user */
trait UserTraceability[Rdf <: RDF, DATASET]
  extends FormModule[Rdf#Node, Rdf#URI]
    with TimeSeries[Rdf, DATASET] {

  def addUserInfoOnTriples(
                            formSyntax: FormSyntax)(
                            implicit graph: Rdf#Graph,
                            lang: String = "en") : FormSyntax = {
    try {
    logger.info("XXXXXXXXXXXXXXXX addUserInfoOnTriples")
    val metadata = getMetadataAboutSubject(formSyntax.subject)
    val resultsUser = mutable.Map[(String,String), String]()
    val resultsTimestamp = mutable.Map[(String,String), Long]()

    for (row: Seq[Rdf#Node] <- metadata){
      logger.info(row)

      // each row: property, object, timestamp, user
      val propertyId =0; val objectId=1; val timestampId=2; val userId=3;

      val timeElementStr = row(timestampId).toString
      // TODO there is something smarter in Banana:
      val timeElement  = timeElementStr.splitAt(timeElementStr.indexOf("^"))._1.replaceAll("\"","").toLong
      val property = row(propertyId).toString
      val objet = row(objectId).toString

      if(resultsTimestamp.contains( (property,objet)) ){
        logger.info(resultsTimestamp( (property,objet) )
        + (" < ")
        + (timeElement)
        + (" = ")
        + (resultsTimestamp((property,objet)) < timeElement))
        if(resultsTimestamp((property,objet)) < timeElement){
          resultsTimestamp += ( (property,objet) -> timeElement)
          resultsUser put ((property, objet), row(userId).toString)
        }
      }
      else{
        resultsTimestamp put ( (property,objet), timeElement)
        // PENDING : already done above:
        resultsUser put ((property, objet), row(userId).toString)
      }
    }
    for (elem <- resultsTimestamp){
    	logger.info("\t" + elem)
    }

    logger.debug(s"YYYYYYYY Before add User Info\n${formSyntax.fields.mkString("\n")}\n")
    val entries =
      for (field: Entry <- formSyntax.fields) yield {
      if (resultsUser.contains( (field.property.toString, field.value.toString) ) ){
    	  logger.debug(s"ZZZZ add User Info ${field.label} ${field.value}")
        field.copyEntry(
            fromMetadata = resultsUser( (field.property.toString, field.value.toString)),
            fromTimeMetadata = resultsTimestamp.getOrElse( (field.property.toString, field.value.toString), 0 ) )
      } else {
        addUserFromGraph(field)
      }
    }
    formSyntax . fields = entries
    formSyntax
    }
    catch {
      case t: Throwable =>
        logger.error( "addUserInfoOnTriples" + t.getLocalizedMessage )
        t.printStackTrace(System.err)
        return formSyntax
    }
  }

  private def addUserFromGraph(field: Entry): Entry = {
    try {
    val resMainDatabase = {
      if (nodeToString(field.value) != "") {
        val queryMainDatabase = s"""
      SELECT ?USER
      WHERE {
        GRAPH ?USER {
         <${field.subject}> <${field.property}> ${makeTurtleTerm(field.value)} . }
      } """
        logger.debug(s"addUserFromGraph: queryMainDatabase $queryMainDatabase")
        sparqlSelectQueryVariables(
          queryMainDatabase,
          Seq("USER"),
          dataset)
      } else List(Seq())
    }
    logger.debug(s"addUserFromGraph: resMainDatabase $resMainDatabase")
    val fieldWithUsers = for (
      row <- resMainDatabase;
      node <- row
    ) yield {
      logger.info(s"addUserFromGraph: ?USER node ${node.getClass()} $node from subject <${field.subject}>")
      field.copyEntry(fromMetadata = node.toString)
    }
    fieldWithUsers.headOption.getOrElse(field)
    } catch {
      case t: Throwable =>
        System.err.println( s"ERROR in addUserFromGraph($field)" )
        t.printStackTrace()
        field
    }
  }
}