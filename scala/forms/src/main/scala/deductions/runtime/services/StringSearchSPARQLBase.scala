package deductions.runtime.services

import deductions.runtime.utils.Configuration
import org.w3.banana.RDF
import deductions.runtime.utils.RDFHelpers
import deductions.runtime.utils.RDFPrefixes

/** common code to StringSearchSPARQL and Lookup */
trait StringSearchSPARQLBase[Rdf <: RDF]
    extends RDFPrefixes[Rdf]
    with RDFHelpers[Rdf] {

  val config: Configuration

   /** fragment of SPARQL for text Query, with or without text:query
    *  see https://jena.apache.org/documentation/query/text-query.html */
   private def textQuery(search: String) =
    if (search.length() > 0) {
      val searchStringPrepared = prepareSearchString(search).trim()
      if (config.useTextQuery)
        s"?thing text:query ( '$searchStringPrepared' ) ."
      else
        s"""    ?thing ?P1 ?O1 .
              FILTER ( isLiteral( ?O1) )
              FILTER ( regex( str(?O1), '.*$searchStringPrepared.*', "i" ) )
            """
    } else ""

  private def classCriterium(classe: String): String = {
		println(
		  s"""classCriterium: classe: "${classe}" """)
    if (classe == "")
      "?CLASS"
    else
      "<" + expandOrUnchanged(classe) + ">"
    }

  private def classVariableInSelect(classe: String): String = {
    if (classe == "")
      "?CLASS"
    else
      ""
  }

  // UNUSED
  def queryWithlinksCountNoPrefetch(search: String,
                                    classe: String = "") = s"""
         |${declarePrefix(text)}
         |${declarePrefix(rdfs)}
         |SELECT DISTINCT ?thing (COUNT(*) as ?count) WHERE {
         |  graph ?g {
         |    ${textQuery(search)}
         |    ?thing ?p ?o .
         |    ?thing a ${classCriterium(classe)} .
         |  }
         |}
         |GROUP BY ?thing
         |ORDER BY DESC(?count)
         |LIMIT 10
         |""".stripMargin

  /** query With links Count, with or without text query */
  def queryWithlinksCount(search: String,
                          classe: String = "") = s"""
         |${declarePrefix(text)}
         |${declarePrefix(rdfs)}
         |${declarePrefix(form)}
         |SELECT DISTINCT ?thing ?COUNT ${classVariableInSelect(classe)} WHERE {
         |  graph ?g {
         |    ${textQuery(search)}
         |    ?thing a ${classCriterium(classe)} .
         |  }
         |  OPTIONAL {
         |   graph ?g1 {
         |    ?thing form:linksCount ?COUNT.
         |  } }
         |}
         |ORDER BY DESC(?COUNT)
         |LIMIT 10
         |""".stripMargin

  // UNUSED
  def queryWithoutlinksCount(search: String,
                             classe: String = "") = s"""
         |${declarePrefix(text)}
         |${declarePrefix(rdfs)}
         |SELECT DISTINCT ?thing WHERE {
         |  graph ?g {
         |    ${textQuery(search)}
         |    ?thing ?p ?o .
         |    ?thing a ${classCriterium(classe)} .
         |  }
         |}
         |LIMIT 15
         |""".stripMargin

  /** prepare Search String: trim, and replace ' with \' */
  private def prepareSearchString(search: String) = {
    search.trim().replace("'", """\'""")
  }
}