package tc {

    // this singleton object is the application
    object SETransform {
      import java.io._
      import org.jsoup._

      type ItemMap = Map[ String, String ]

      // create temp dir for storing downloaded and generated files:
      def createTempDir(): java.io.File = {
        import java.io._

        val tmpBaseDir = "/tmp"
        
        // let's always just use the same directory for a given rundate:
        val dt = new java.util.Date()
        val dirName = String.format( "tcse-%tF", dt) + ".d"

        val tmp = new File( tmpBaseDir + "/" + dirName )

        // may fail if already exists
        tmp.mkdir()
  
        tmp
      }

      // Parse Listings from array of text strings into
      // array of items
      def parseListings( srcLines: Iterator[String] ): Array[ItemMap] = {
        // skip one header line 
        // TODO: Use to verify record count:
        val topLine = srcLines.next
        
        val headerLine = srcLines.next
        // println( "Header line: ", headerLine )
        val headers = headerLine.split( "\t" ).map( s => s trim )
        /*
        println( "Parsed headers: ")
        for ( hdr <- headers )
          println( "  " + hdr )
        */
        val entriesBuf = scala.collection.mutable.ArrayBuffer.empty[ ItemMap ]

        while( srcLines.hasNext )
        {
          val entryLine = srcLines.next
          // break at tabs, strip off surrounding quotes:
          val entryVals = entryLine.split( "\t" ).map( s => s.stripPrefix( "\"" ).stripSuffix( "\"" ) )

          val entryMap = ( headers zip entryVals ).toMap
          // println( "entry: " + entryMap )
          entriesBuf += entryMap
        }
        entriesBuf.toArray
      }

      val ncf = java.text.NumberFormat.getCurrencyInstance()
      ncf.setMaximumFractionDigits( 0 )

      val df = new java.text.SimpleDateFormat( "MM/dd/yyyy" ) 

      val inf = java.text.NumberFormat.getIntegerInstance()


      def entryToXML( imgMap: Map[ String, scala.xml.NodeSeq ], pubLinkMap: Map[ String, String ], item: ItemMap ) : scala.xml.Elem = {

        // Apply specified formatting function only if s is non-empty
        def cfmt( f : String => String , s : String ) = 
            if( s.length > 0 ) f( s ) else ""  

        val uriStr = item( "URI" )  // That's to the 

        // Look up uriStr in imgMap to extract img NodeSeq
        val imgNodeSeq = imgMap( uriStr )
        val pubLinkOpt = pubLinkMap.get( uriStr )
        val imgLink = pubLinkOpt match {
          case Some( pubURI ) => <a href={ pubURI }>{ imgNodeSeq }</a>
          case _ => imgNodeSeq
        }
        
        val addrStr = item( "Address" ) + " " + item( "Unit" )
        val addrLink = pubLinkOpt match {
          case Some( pubURI ) => <h3><a href={ pubURI }>{ addrStr }</a></h3>
          case _ => <h3>{ addrStr }</h3>
        }
        val recLink = <a href={ uriStr }>Sale Record</a>

        val salePriceStr = ncf.format( item( "Sale Price" ) toDouble )
        val saleDateStr = df.format( new java.util.Date( item( "Sale Date" ) ) )
        
        val bedCountStr = cfmt( s => inf.format( s toDouble ) + " beds", item( "Bedrooms" ) )
        val bathCountStr = cfmt( s => "   " + inf.format( s toDouble ) + " baths", item( "Baths" ) )
        val unitTypeStr = cfmt( s => s, item( "Unit Type" ) )
        val sqFtStr = cfmt( s => "   " + inf.format( s toDouble ) + " ft²", item( "Sqft" ) )

        // neighborhood hopefully redundant for this project...
        // val nabeStr = cfmt( s => s, item( "Neighborhood" ) )

        val itemXML =
          <div class="item compact">
            <div class="item_inner">
              <div class="photo">
                { imgLink }
              </div>
              <div class="more">
                <h3>{ salePriceStr  }</h3>
                  { saleDateStr }
                  { bedCountStr }{ bathCountStr }{ sqFtStr }
              </div>
              <div class="body">
                { addrLink }
                { unitTypeStr }
                { recLink } ( streeteasy insiders only )
              </div>
            </div>
          </div>   
        itemXML
      }

      // given array of XML elements for each entry, make body content suitable for pasting in to WordPress.
      // Will need to wrap this in an XHTML doc for local testing
      def makeBodyContent( xmlEntries: Array[ scala.xml.Elem ] ) : scala.xml.Elem = {
        val dt = new java.util.Date()
        val dateStr = String.format( "%tD", dt)

        val document =
          <div id = "body-content">
            <p>Courtesy of <a href="http://streeteasy.com/" target="_blank">StreetEasy</a>, these are the properties whose sales were "recorded" by the city in the past seven days ( ending {dateStr} ) - the sales may have taken place earlier.</p>
            <div class="items compact">
            { scala.xml.NodeSeq fromSeq xmlEntries } 
            </div>
          </div>

        document
      }

      // Wrap body content in a full XHTML document for local viewing / testing:
      def makeHtmlDoc( bodyContent: scala.xml.Elem ) : scala.xml.Elem = {
        val document =
<html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="en-US" xmlns:og="http://ogp.me/ns#" xmlns:fb="http://www.facebook.com/2008/fbml">

<head profile="http://gmpg.org/xfn/11">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<link rel="stylesheet" href="http://tribecacitizen.com/wp-content/themes/tribeca/style.css" type="text/css" media="screen,projection" />
<link rel="stylesheet" href="listings-subset.css" type="text/css" media="screen,projection" />


<title>TEST Tribeca Citizen |   Recent Real Estate Sales in Tribeca</title>
</head>

<body>
<div id="content-single"> 
  
  <div id="post-46204" class="post-46204 post type-post status-publish format-standard hentry category-real-estate tag-101-warren tag-110-duane tag-165-duane tag-17-jay tag-200-chambers tag-25-murray tag-25-n-moore tag-275-grenwich tag-415-greenwich tag-45-lispenard tag-475-greenwich tag-53-leonard tag-55-warren tag-57-reade tag-77-reade tag-greenwich-court tag-recent-real-estate-sales-in-tribeca tag-streeteasy tag-tribeca-space tag-tribeca-summit tag-zinc-building style">
  
    <h2 class="posttitle">Recent Real Estate Sales in Tribeca</h2>
    
    <p class="postmetadata"><a href="http://tribecacitizen.com/category/real-estate/" title="View all posts in Real Estate" rel="category tag">Real Estate</a></p>
    
    <div class="entry clearfloat">
      { bodyContent }
    </div>
  </div>
</div>

<hr/>

</body>
</html> 
        document
      }

      // saveResults -- save the XHTML for the resulting document
      def saveResults( destFile: String, doc: scala.xml.Elem, writeDocType: Boolean = true ) = {
        // val w = new java.io.FileWriter( destFile )
        val w = new java.io.PrintWriter( destFile, "UTF-8" )
        val doctype: scala.xml.dtd.DocType = 
          if( writeDocType ) scala.xml.dtd.DocType("html",
                                                   scala.xml.dtd.PublicID("-//W3C//DTD XHTML 1.0 Strict//EN",
                                                                          "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"), 
                                                   Nil) else null
        xml.XML.write( w, doc, "UTF-8", xmlDecl = false, doctype = doctype )
        w.close()
        // scala.xml.XML.save( destFile, docTemplate, /* xmlDecl = true, */ enc = "utf-8" )
      }

      def runCmd( wd: java.io.File, cmd: String ) = {
        import scala.sys.process._

        val pb = Process( cmd, Some( wd ) )
        
        println( "executing command: " )
        println( cmd )
        println()
        val p = pb.run()

        val ev = p.exitValue()

        println()
        println( "command completed with exitcode: " + ev )

      }

      // login to streetEasy and save the cookie...
      def seLogin( wd: java.io.File ) = {
        // val cmd = "wget --keep-session-cookies --no-check-certificate --save-cookies cookies.txt -O login.out --post-data 'do_login=true&remember=true&login=tribecacitizen&password=madonna&do_login=login+%C2+%BB' https://streeteasy.com/nyc/user/login"
        val cmd = "curl --cookie-jar logincookies.txt -o login-output.txt --data 'do_login=true&remember=true&login=tribecacitizen&password=madonna&do_login=login+%C2+%BB' https://streeteasy.com/nyc/user/login"
        runCmd( wd, cmd )
      }

      def seFetchURI( wd: java.io.File, uri: String, dstFile: String = "" ) = {
        val dstArg = if( dstFile.length > 0 ) ( "-o " + dstFile ) else ""
        // val template = "wget --keep-session-cookies --no-check-certificate --load-cookies cookies.txt %s %s"
        val template = "curl --cookie logincookies.txt %s %s"

        val cmd = String.format( template, dstArg, uri )

        runCmd( wd, cmd )
      }

      val testString = "Hello, Tribeca!"

      // Main driver:  Given a specific set of FetchArgs, do what's needed to obtain
      // a FetchResult 
      def fetch( fa: FetchArgs) : FetchResult = {

        import java.io._

        println( testString )

        val wdir = createTempDir()

        val inDir = new File( wdir.getAbsolutePath() + "/in" );
        inDir.mkdir()  // don't worry if it exists!
        val inDirPath = inDir.getAbsolutePath()

        val outDir = new File( wdir.getAbsolutePath() + "/out" );
        outDir.mkdir() // don't worry if it exists
        val outDirPath = outDir.getAbsolutePath()
        
        println( "Created temp dir " + wdir.getAbsolutePath() )

        // log in:
        seLogin( inDir )

        // fetch the TSV file:
        val tsvURI = "http://streeteasy.com/nyc/process/closings/xls/area:105%7Crecorded%3C7"
        val tsvFile = "recorded-sales.tsv" 
        seFetchURI( inDir, tsvURI, tsvFile )

        // fetch the RSS XML file:
        val rssURI = "http://streeteasy.com/nyc/closings/rss/area:105%7Crecorded%3C7?t=1125016%3A43ec24d4d7d1b990"
        val rssXmlFile = "recorded-sales-rss.xml"
        seFetchURI( inDir, rssURI, rssXmlFile )

        // read the TSV file we get from exporting:
        val tsvSrcLines = scala.io.Source.fromFile( inDirPath + "/" + tsvFile ).getLines()
        println( "read listings from " + tsvFile )

        // read the XML file we get from RSS:
        val xmlListingsElem = scala.xml.XML.loadFile( inDirPath + "/" + rssXmlFile )
        println( "read listings from " + rssXmlFile )

        // extract guid and img pair from the specified item
        def getGuidAndImg( item: scala.xml.Node ) : (String, scala.xml.NodeSeq) = {
            val guidStr = (item \ "guid").text

            // REALLY annoying but the content in description is an HTML fragment that
            // we have to clean and re-parse.  yuck!
            // FIXME:  Now that we're using jsoup anyway, should probably just use that here...
            val descStr = (item \ "description").text.replaceAll("&","")
            // println( "Description: " + descStr )
            val descElem = scala.xml.XML.loadString( "<fragment>" + descStr + "</fragment>" )

            // img in RSS feed, which is bigger than we want
            val xmlImgElem = descElem \ "img" 
            val imgSrc = xmlImgElem \ "@src"
          
            val imgElem = <img class="tiny" src={imgSrc} width="48" height="48" border="0" />

            ( guidStr, imgElem )
        }

        val listingItems = xmlListingsElem \\ "item"
        val listingEnts = listingItems.map( getGuidAndImg )

        // Construct a Map from GUID string (really the URI) to img element
        val listImgMap = listingEnts.toMap

        // Now let's fetch the closing entries for all the GUIDs:
        val listingURIs = listingEnts.map( _._1 )

        val lmapEnts = scala.collection.mutable.ArrayBuffer.empty[ ( String, String ) ]

        for( uri <- listingURIs ) {
          val baseID = uri.substring( uri.lastIndexOf( '/' ) + 1 )
          val saleDocFileName = baseID + ".html"
          seFetchURI( inDir, uri, saleDocFileName )

          // now let's try and pick out the listing component and get to the basic listing:
          val saleDocFile = new File( inDir + "/" + saleDocFileName )
          
          println( "extracting public listing from " + saleDocFileName )
          try {
            val saleDoc = Jsoup.parse( saleDocFile, "utf-8" )
            val listingElem = saleDoc.getElementsByClass( "listing" ).first()
            val linkElem = listingElem.getElementsByTag( "a" ).first()
            val linkHRef = linkElem.attr("href")
            val publicURI = "http://streeteasy.com" + linkHRef

            println( "public URI: " + publicURI )
            
            lmapEnts += Tuple2( uri, publicURI )
          } catch {
            case x : Throwable => println( "Caught exception while extracting public listing, ignoring..." )
          }

        }
        // map private to public URIs:
        val pubLinkMap = lmapEnts.toArray.toMap

        val entries = parseListings( tsvSrcLines )
        val xmlEntries = entries map ( entryToXML( listImgMap, pubLinkMap, _ ) )

        val bodyContent = makeBodyContent( xmlEntries )
        val destContentFile = outDirPath + "/wp.xhtml";
        println( "Writing body content to " + destContentFile );
        saveResults( destContentFile, bodyContent, false )

        val resDoc = makeHtmlDoc( bodyContent )
        val destFile = outDirPath + "/out.xhtml";
        println( "Writing full XHTML output to " + destFile );
        // println( resDoc );

        saveResults( destFile, resDoc )

        // and dump the rss file in there:
        val rssCpCmd = String.format( "cp listings-subset.css %s", outDirPath )
        runCmd( new File( "." ), rssCpCmd )

        val res = new FetchResult( fa, pf = destFile, wpf = destContentFile )

        res
      }

      def main(args: Array[String]): Unit = {
        val fa = new FetchArgs

        fetch( fa )
      }
    }
}
