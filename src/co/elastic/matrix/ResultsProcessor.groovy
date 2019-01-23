package co.elastic

/**
  Process the result map variable and build and html report.
*/
public processResults(results){
  sh 'curl -sLO https://code.jquery.com/jquery-3.3.1.slim.min.js'
  sh 'curl -sLO https://cdn.jsdelivr.net/npm/htmlson.js@1.0.4/src/htmlson.js'
  def jquery = readFile(file: 'jquery-3.3.1.slim.min.js')
  def htmlson = readFile(file: 'htmlson.js')
  String html = """<html>
  <head>
    <title>Integration Test Results</title>
  </head>
  <script type="text/javascript">${jquery}</script>
  <script type="text/javascript">${htmlson}</script>
  <style>
    table {
      font-family: Arial, Helvetica, sans-serif;
      border: 1px solid #000000;
      background-color: #EEEEEE;
      width: 50em;
      text-align: center;
      border-collapse: collapse;
      margin: 30px 20px;
    }
    table td, table th {
      border: 1px solid #AAAAAA;
      padding: 3px 2px;
    }
    table tbody td {
      font-size: 13px;
    }
    table tr:nth-child(even) {
      background: #D0E4F5;
    }
    table thead {
      background: #1C6EA4;
      background: -moz-linear-gradient(top, #5592bb 0%, #327cad 66%, #1C6EA4 100%);
      background: -webkit-linear-gradient(top, #5592bb 0%, #327cad 66%, #1C6EA4 100%);
      background: linear-gradient(to bottom, #5592bb 0%, #327cad 66%, #1C6EA4 100%);
      border-bottom: 2px solid #444444;
    }
    table thead th {
      font-size: 15px;
      font-weight: bold;
      color: #FFFFFF;
      text-align: center;
      border-left: 2px solid #D0E4F5;
    }
    table thead th:first-child {
      border-left: none;
    }

    table tfoot td {
      font-size: 14px;
    }
    table tfoot .links {
      text-align: right;
    }
    table tfoot .links a{
      display: inline-block;
      background: #1C6EA4;
      color: #FFFFFF;
      padding: 2px 8px;
      border-radius: 5px;
    }

    .error {
      color: red;
    }

    .ok {
      color: green;
    }
  </style>
  <body>
  """

  results.each{ k, v ->
    def records = []
    v.data.each{ dk, dv ->
      def row = [:]
      v.y.each{ vy ->
        row.put(vy, "N/A")
      }
      row.putAll(dv)
      records.add(row)
    }

    String jsonRecords = toJSON(records).toString()
    html += """
    <h2>Agent ${v.name}</h2>
    <table class="${k}Agent"></table>
    <script type="text/javascript">
      let ${k}Data = ${jsonRecords};
      let ${k}Table = \$('.${k}Agent').htmlson({data: ${k}Data});
    </script>
    """
  }

  html += '''
  <script type="text/javascript">
    $('td').each(function(){
      if(this.textContent === "1"){
        $( this ).replaceWith("<td class='ok'>OK</td>");
      } else if(this.textContent === "0") {
        $( this ).replaceWith("<td class='error'>ERROR</td>");
      }
    });
  </script>
  </body>
  </html>
  '''
  writeFile(file: 'results.html', text: html)
}

return this
