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
                                $categorie[$row[0]]=0;
                        }
                    ?>                 
                 </ul>
                 
                 <ul class="nav navbar-nav navbar-right"><li><a href="confronta_notizie.php">CONFRONTA NOTIZIE!</a></li></ul>
                 
             </div><!-- /.navbar-collapse -->
             
             </div><!-- /.container-fluid -->
         </nav>
         <div class="col-md-1"></div>
         <div class="col-md-8">
            <div class="page-header"><h2>Confronta notizie</h2></div>            
            <form action='confronta_notizie_result.php' method='get'>                
                <br><br><label for='argomenti'>Argomenti: (<i>Inserisci gli argomenti separati da una virgola)</i></label>
                <input class="form-control" type="text" id="argomenti" name="argomenti">
                <br>Argomenti più popolari: (<i>clicca per aggungere</i>)
                <div>
                    <?php
                    mysql_query("SET CHARACTER SET 'utf8'");
                    $query =  "select tag from(SELECT count(*) as 'n', tag FROM `contiene` group by contiene.tag order by n DESC) conts limit 0,10;";
                    $res = mysql_query($query);
                    $n = mysql_num_rows($res);
                    $cont = 0;
                    echo"<div class='col-md-3 list-group'>";
                    for($i=0; $i<$n;$i++){
                        $row = mysql_fetch_row($res);
                        echo"<a class='list-group-item'id='".$cont."' onclick='addArg(".$cont.")'>".$row[0]."</a>";
                        $cont++;
                    }                    
                    echo"</div>";
                    
                    mysql_freeresult($res);
                    mysql_query("SET CHARACTER SET 'utf8'");
                    $query =  "select tag from(SELECT count(*) as 'n', tag FROM `contiene` group by contiene.tag order by n DESC) conts limit 10,10;";
                    $res = mysql_query($query);
                    $n = mysql_num_rows($res);
                    echo"<div class='col-md-3 list-group'>";
                    for($i=0; $i<$n;$i++){
                        $row = mysql_fetch_row($res);
                        echo"<a class='list-group-item'id='".$cont."' onclick='addArg(".$cont.")'>".$row[0]."</a>";
                        $cont++;
                    }                    
                    echo"</div>";
                    
                    ?>
                        
                </div>
                
                Data notizia: (<i>se non inserita il risultato della ricerca sarà svolto solo per argomenti</i>)<br>
                <br>
                <div class='col-md-6'>
                    <div class='col-md-7'><label for='giorno'>Giorno</label></div><div class='col-md-5'><input name='giorno' type='number' id='giorno' class='form-control'></div>
                    <br><div class='col-md-7'><label for='mese'>Mese</label></div><div class='col-md-5'><input name='mese' type='number' id='mese' class='form-control'></div>
                    <br><div class='col-md-7'><label for='anno'>Anno</label></div><div class='col-md-5'><input name='anno' type='number' id='anno' class='form-control'></div>
                    <br><br>
                    <div class='col-md-10'></div>
                    <button type='submit' class='btn btn-default'>VAI</button>
                </div>
            </form>
            
            
            <script>
                function addArg(cont){
                    if (document.getElementById('argomenti').value=='') {
                        document.getElementById('argomenti').value+=document.getElementById(cont).innerHTML;
                    }else{
                        document.getElementById('argomenti').value+=","+document.getElementById(cont).innerHTML;
                    }                    
                }
            </script>
         </div>