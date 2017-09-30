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
                        $categorie = array();
                        for($i = 0; $i<$num_rows1; $i++){
                            $row = mysql_fetch_row($result1);                           
                                print("<li><a href='category.php?selected_category=".$row[0]."'>".$row[0]."</a></li>");
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
            $date = '';
            if($_GET['anno']!='' && $_GET['mese']!='' && $_GET['giorno']!=''){
                $date = $_GET['anno'].'-'.$_GET['mese'].'-'.$_GET['giorno'];
            }
            
            $tok= strtok($_GET['argomenti'],",");
            $args[0] = $tok;
            $cont = 1;
            while($tok!==false){
                $tok = strtok(",");
                $args[$cont] = $tok;
                $cont++;
            }
            $n = count($args);
            echo"<div class='page-header'><h2>Argomenti:";
            for($i=0; $i<$n; $i++){
                if($args[$i]==''){
                    $n--;
                    break;
                }
                echo " <a href='argomenti.php?argomenti=".$args[$i]."'>".$args[$i]."</a>";
            }
            if($date==''){
                echo ",Data: oggi";
            }else{
                echo ",Data: ".$date;
            }
            echo"</h2></div>";
            
            $query = "select distinct Link, Title, Summary, Image_path, categoria, pubdate, sito from articolo, contiene ";
            if($n>0){
                $query = $query."where ";
            }
            //TITLE
            for($i=0; $i<$n; $i++){
                if($i<$n-1){
                    $query = $query."title like '% ".$args[$i]." %' or title like '% ".ucwords($args[$i])." %' and ";
                }else{
                    $query = $query."title like '% ".$args[$i]." %' or title like '% ".ucwords($args[$i])." %' or ";
                }
            }
            
            
            //SUMMARY
            for($i=0; $i<$n; $i++){
                if($i<$n-1){
                    $query = $query."summary like '% ".$args[$i]." %' or summary like '% ".ucwords($args[$i])." %' and ";
                }else{
                    $query = $query."summary like '% ".$args[$i]." %' or summary like '% ".ucwords($args[$i])." %' ";
                }
            }
                  
            
            for($i=0; $i<$n; $i++){
                    $query = $query." or contiene.tag = '".$args[$i]."' and contiene.articolo = articolo.link ";                
            }
            
            if($date==''){
                 $query = $query." and pubdate = curdate() order by categoria, punti;";
            }else{
                $query = $query." and pubdate = '".$date."' order by categoria, punti;";
            }
                mysql_query("SET CHARACTER SET 'utf8'");
                $res = mysql_query($query);
                $n =mysql_num_rows($res);
                for($j = 0; $j<$n; $j++){
                    $art = mysql_fetch_row($res);                    
                            print("<div class='panel panel-default'>");
                            print("<div class='panel-heading'><h2>");
                            echo "<a href='articolo.php?articolo=".$art[0]."'>";
                            echo $art[1];
                            echo "</a>";
                            print("</h2></div>");
                            echo "<div class='panel-body'>";
                            echo "<img src='".$art[3]."'></img>";
                            echo "<br><b>".$art[2]."</b>";
                            echo"</div>";
                            echo"<div class='panel-footer'><div align='right'><a href='".$art[6]."'>".$art[6]."</a>, ".$art[5]."</div></div>";
                            print("</div>");
                }
            
            
            
            ?>
            
         </div>
          <div class="col-md-4">
            <div class="panel panel-default">
                <div class="panel-heading">
                    Tag più popolari:
                </div>
                <div class="panel-body">
                    <?php                    
                    mysql_query("SET CHARACTER SET 'utf8'");
                    $query = "select tag from(SELECT count(*) as 'n', tag FROM `contiene` group by contiene.tag order by n DESC) conts limit 0,15;";
                    mysql_free_result($res);
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
         