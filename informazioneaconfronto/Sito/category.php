 <html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>Informazione a confronto</title>
        <link href="css/bootstrap.min.css" rel="stylesheet"> 
        <link rel="icon" href="favicon.png" sizes="32x32" type="image/png">        
    </head>
    
    <body class="col-md-12">
        
        <center><img src="Logo.jpg"></center>
         <nav class="navbar navbar-default">
             <div class="container-fluid">    
                 <div class="navbar-header">
                     <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
                         <span class="sr-only">Toggle navigation</span>
                         <span class="icon-bar"></span>
                         <span class="icon-bar"></span>
                         <span class="icon-bar"></span>
                     </button>
                     <a class="navbar-brand" href="index.php">HOME</a>
                 </div>
 
    <!-- Collect the nav links, forms, and other content for toggling -->
             <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                 <ul class="nav navbar-nav">
                    <?php
                        mysql_query("SET CHARACTER SET 'utf8'");
                        $conn = mysql_connect("localhost","taschin.federico","Federico1996") or die("Error connection");
                        mysql_selectdb("rassegna_stampa");
                        $query1 = "select Nome from categoria";
                        $result1 = mysql_query($query1);
                        $num_rows1 = mysql_numrows($result1);
                        for($i = 0; $i<$num_rows1; $i++){
                            $row = mysql_fetch_row($result1);   
                            if($_GET['selected_category']==$row[0]){
                                print("<li class='active'><a href='category.php?selected_category=".$row[0]."'>".$row[0]."</a></li>");
                            }else{                                                    
                                print("<li><a href='category.php?selected_category=".$row[0]."'>".$row[0]."</a></li>");
                            }
                        }
                    ?>
                 </ul>
                 <ul class="nav navbar-nav navbar-right"><li><a href="confronta_notizie.php">CONFRONTA NOTIZIE!</a></li></ul>
             </div><!-- /.navbar-collapse -->
             </div><!-- /.container-fluid -->
         </nav>
         <div class="col-md-1"></div>
         <div class="col-md-7">
         <?php
                        mysql_query("SET CHARACTER SET 'utf8'");
                        $query2 = "select Link, Title, Summary, Image_path, pubdate, sito from articolo where Categoria = '".$_GET['selected_category']."' order by pubdate desc, punti desc";
                        $result2 = mysql_query($query2);
                        $num_rows2 = mysql_num_rows($result2);
                        for($i = 0; $i<$num_rows2; $i++){
                            $row = mysql_fetch_row($result2);
                            print("<div class='panel panel-default'>");
                            print("<div class='panel-heading'><h2>");
                            echo "<a href='articolo.php?articolo=".$row[0]."'>";
                            echo $row[1];
                            echo "</a>";
                            print("</h2></div>");
                            echo "<div class='panel-body'>";
                                 echo "<img src='".$row[3]."'></img>";
                                 echo "<br><b>".$row[2]."</b>";
                            echo"</div>";
                            echo"<div class='panel-footer'><div align='right'><a href='".$row[5]."'>".$row[5]."</a>, ".$row[4]."</div></div>";
                            print("</div>");
                        }
         ?>
         </panel>
         </div>
         <div class="col-md-4">
            <div class="panel panel-default">
                <div class="panel-heading">
                    Tag più popolari:
                </div>
                <div class="panel-body">
                    <?php
                    $query = "select tag from(SELECT count(*) as 'n', tag FROM `contiene` group by contiene.tag order by n DESC) conts limit 0,15;";
                    $res = mysql_query($query);
                    $n = mysql_num_rows($res);
                    for($i=0; $i<$n;$i++){
                        $row = mysql_fetch_row($res);
                        echo "<a href='tag.php?tag=".$row[0]."'>".$row[0]." </a> - ";
                    }                    
                    ?>
                </div>
            </div>
            
            <div class="panel panel-default">
                <div class="panel-heading">
                    Ricerca argomenti: 
                </div>
                <div class="panel-body">
                    <form action="argomenti.php" action="get">
                        Inserire gli argomenti separati da una virgola
                        <br>
                        <input type="text" name="argomenti" class="form-control">
                        <br>
                        <center><button class="btn btn-default" type="submit">Cerca</button></center>
                    </form>
                </div>  
         </div>
         </div>
         
    </body>

</html>