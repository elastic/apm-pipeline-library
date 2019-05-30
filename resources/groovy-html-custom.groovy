<!DOCTYPE html>
<html lang="en">

<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta charset="UTF-8">
  <title>Email</title>
</head>

<body style="margin: 0; padding: 0;">
  <table logopacing="0" style="table-layout: fixed;max-width: 90%;width: 800px;border-collapse: collapse;margin: 0 auto;">
    <thead style="background-color: #FFFFFF;">
      <tr>
        <td colspan="7" style="height: 32px;"></td>
      </tr>
      <tr>
        <td rowspan="2" colspan="2" style="padding: 0 0 10px 0;width: 156px;text-align: center;">
          <img alt="logo" src="https://avatars0.githubusercontent.com/u/6764390?s=100&v=4">
        </td>
        <td colspan="3" style="font-family: Helvetica; font-size: 12px; color: #343B49; letter-spacing: 2px; line-height: 16px; text-transform: uppercase;">
          Observability-robots
        </td>
        <td rowspan="2" colspan="1" style="padding-left: 10px;"></td>
      </tr>
      <tr>
        <td colspan="4" style="letter-spacing:1px;font-weight:700;font-size:24px;line-height:30px;font-family:,Helvetica;padding-right:24px">
          ${statusSuccess ? 'The job is done' : "The job couldn't be done"}
        </td>
      </tr>
      <tr>
        <td colspan="1" style="height: 10px; width: 30px"></td>
        <td colspan="2" style="height: 10px;"></td>
        <td colspan="3" style="height: 10px;"></td>
        <td colspan="1" style="height: 10px; width: 100px"></td>
      </tr>
    </thead>
    <tbody style="background-color: #F4F4F4;">
      <tr>
        <td colspan="1"></td>
        <td colspan="2" style="height: 24px;position: relative;">
        </td>
        <td colspan="4"></td>
      </tr>
      <!--TABLE_BUILD-->
      <tr>
        <td rowspan="3" colspan="1" style="text-align: center;">
          <%if(statusSuccess){%>
            <img alt="jenkins" src="https://avatars0.githubusercontent.com/u/107424?s=100&v=4" style="max-height: 100%;max-width: 100%;width: 100px;">
          <%} else {%>
            <img alt="jenkins" src="https://raw.githubusercontent.com/jenkinsci/jenkins/master/war/src/main/webapp/images/rage.png" style="max-height: 100%;max-width: 100%;width: 100px;">
          <%}%>
        </td>
        <td colspan="2" style="vertical-align: bottom;font-family: Georgia; font-weight: bold; font-size: 16px; color: #343B49; letter-spacing: 1px; line-height: 16px;">
          Jenkins
        </td>
        <td rowspan="3" colspan="1"></td>
        <td rowspan="3" colspan="3" style="padding-right: 24px;">
          <table logopacing="0" cellpadding="0" style="text-align: right;width:90%">
            <tr>
              <td width="64px" height="142px">
                <a href="${jobUrl}" style="text-decoration: none;border-radius: 50%;background-color: #ffffff;display: inline-block;height: 72px;width: 72px;line-height: 76px;text-align: center">
                  <img alt="arrow" style="max-height: 100%;max-width: 100%;height: 100px;" src="http://clipart-library.com/images/Bigr66BgT.png">
                </a>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td colspan="2" style="vertical-align: middle; font-family: Helvetica; font-size: 24px; color: #343B49; letter-spacing: 1px; line-height: 30px;">
          ${jenkinsText}
        </td>
      </tr>
      <tr>
        <td colspan="2" style="vertical-align: baseline; font-family: Lato, Helvetica; font-size: 16px; color: ${statusSuccess ? '#00D06D' : '#FF0082'}; letter-spacing: 1px; line-height: 16px; font-weight:900; text-transform: capitalize;">
          ${statusSuccess ? 'successful' : 'failed'}
        </td>
      </tr>
      <tr>
        <td colspan="7" style="height: 32px;"></td>
      </tr>
      <tr>
        <td colspan="7" style="padding-left: 24px;text-align: left; vertical-align: middle; font-family: Helvetica; font-size: 12px; color: #343B49; letter-spacing: 1px;">
          <strong style="font-family: Helvetica; color: #343B49;">Build Cause:</strong> ${build?.causes?.shortDescription}<br>
          <strong style="font-family: Helvetica; color: #343B49;">Start Time:</strong> ${build?.startTime}<br>
          <strong style="font-family: Helvetica; color: #343B49;">Duration:</strong> ${Math.round(build?.durationInMillis/1000/60)} min ${Math.round(build?.durationInMillis/1000)%60} sec<br>
        </td>
      </tr>
      <!--TABLE_BUILD-->
      <tr>
        <td colspan="7" style="height: 32px;"></td>
      </tr>
      <!--TABLE_CHANGES-->
      <tr style="background-color: #F4F4F4;${ changeSet?.size() != 0 ? '' : 'display: none;' }">
        <td colspan="6" style="padding-left: 24px;font-family: Helvetica; font-size: 10px; color: #343B49; letter-spacing: 2px; line-height: 16px; margin-bottom: 0; text-transform: uppercase;">
          Changes
          <hr>
        </td>
        <td colspan="1" style="background-color: #F4F4F4;"></td>
      </tr>
      <tr>
        <td colspan="7" style="padding-left: 24px;border-collapse: collapse;background-color: #F4F4F4;">
          <table logopacing="0" cellpadding="0" style="text-align: left;font-size: 12px;">
          <% changeSet.each{ c -> %>
            <tr>
              <td>
              <strong style="font-family: Helvetica; color: #343B49;">Author:</strong> ${c.author.id}<br>
              <strong style="font-family: Helvetica; color: #343B49;">Full Name:</strong>  ${c.author.fullName}<br>
              <strong style="font-family: Helvetica; color: #343B49;">email:</strong>  ${c.author.email}</br>
              <strong style="font-family: Helvetica; color: #343B49;">Commit:</strong>  <a href="${c.url}">${c.commitId}</a><br>
              <strong style="font-family: Helvetica; color: #343B49;">Message:</strong>  ${c.msg}<br>
              <strong style="font-family: Helvetica; color: #343B49;">Date:</strong>  ${c.timestamp}<br>
              <hr>
              </td>
            </tr>
          <%}%>
          </table>
        </td>
      </tr>
      <!--TABLE_CHANGES-->
      <tr>
        <td colspan="7" style="height: 32px;${ stepsErrors.size() != 0 ? '' : 'display: none;' }"></td>
      </tr>
      <!--TABLE_STEP_ERRORS-->
      <tr style="background-color: #F4F4F4;${ stepsErrors?.size() != 0 ? '' : 'display: none;' }">
        <td colspan="6" style="padding-left: 24px;font-family: Helvetica; font-size: 10px; color: #343B49; letter-spacing: 2px; line-height: 16px; margin-bottom: 0; text-transform: uppercase;">
          Step Errors
          <hr>
        </td>
        <td colspan="1" style="background-color: #F4F4F4;"></td>
      </tr>
      <tr>
        <td colspan="7" style="padding-left: 24px;border-collapse: collapse;background-color: #F4F4F4;">
          <table logopacing="0" cellpadding="0" style="text-align: left;font-size: 12px;table-layout: fixed;width: 90%;">
          <% stepsErrors.findAll{item -> item?.result == "FAILURE"}.each{ c -> %>
            <tr>
              <td>
              <strong style="font-family: Helvetica; color: #343B49;">Name:</strong> ${c.displayName}<br>
              <strong style="font-family: Helvetica; color: #343B49;">Description:</strong>  ${c.displayDescription}<br>
              <strong style="font-family: Helvetica; color: #343B49;">Result:</strong>  ${c.result}<br>
              <strong style="font-family: Helvetica; color: #343B49;">Duration:</strong>  ${Math.round(c.durationInMillis/1000/60)} min ${Math.round(c.durationInMillis/1000)%60} sec</a><br>
              <strong style="font-family: Helvetica; color: #343B49;">Start Time:</strong>  ${c.startTime}</a><br>

              <% c.actions.findAll{item -> item?.urlName == "log"}.each{ l ->%>
                <a href="${jenkinsUrl}/${l._links.self.href}">Log</a><br>
              <%}%>
              <hr>
              </td>
            </tr>
          <%}%>
          </table>
        </td>
      </tr>
      <!--TABLE_STEP_ERRORS-->
      <tr>
        <td colspan="7" style="height: 32px;${ testsSummary?.size() != 0 ? '' : 'display: none;' }"></td>
      </tr>
      <!--TABLE_TEST-->
      <tr style="background-color: #F4F4F4;${ testsSummary?.total != null ? '' : 'display: none;' }">
        <td colspan="6" style="padding-left: 24px;padding-top: 10px;vertical-align: bottom; font-family: Helvetica; font-size: 12px; color: #343B49; letter-spacing: 2px; line-height: 16px; text-transform: uppercase;">
          Test Results
          <hr>
        </td>
        <td colspan="1" style="background-color: #F4F4F4;"></td>
      </tr>
      <tr style="${ testsSummary?.total != null ? '' : 'display: none;' }">
        <td colspan="7" style="padding-left: 24px;text-align: left; border-collapse: collapse;font-size: 12px;">
          <strong style="font-family: Helvetica; color: #343B49;">Failed:</strong>  ${testsSummary.failed}<br>
          <strong style="font-family: Helvetica; color: #343B49;">Passed:</strong>  ${testsSummary.passed}<br>
          <strong style="font-family: Helvetica; color: #343B49;">Skipped:</strong>  ${testsSummary.skipped}<br>
          <strong style="font-family: Helvetica; color: #343B49;">Total:</strong>  ${testsSummary.total}<br>
        </td>
      </tr>
      <!--TABLE_TEST-->
      <tr>
        <td colspan="7" style="height: 32px;${ testsErrors?.size() != 0 ? '' : 'display: none;' }"></td>
      </tr>
      <!--TABLE_TEST_ERRORS-->
      <tr style="background-color: #F4F4F4;${ testsErrors?.size() != 0 ? '' : 'display: none;' }">
        <td colspan="6" style="padding-left: 24px;font-family: Helvetica; font-size: 10px; color: #343B49; letter-spacing: 2px; line-height: 16px; margin-bottom: 0; text-transform: uppercase;">
          Test Errors
          <hr>
        </td>
        <td colspan="1" style="background-color: #F4F4F4;"></td>
      </tr>
      <tr>
        <td colspan="7" style="padding-left: 24px;border-collapse: collapse;background-color: #F4F4F4;">
          <table logopacing="0" cellpadding="0" style="text-align: left;font-size: 12px;table-layout: fixed;width: 90%;">
          <% testsErrors.findAll{item -> item?.status == "FAILED"}.each{ c -> %>
            <tr>
              <td>
              <strong style="font-family: Helvetica; color: #343B49;">Name:</strong> ${c.name}<br>
              <strong style="font-family: Helvetica; color: #343B49;">Status:</strong>  ${c.status}<br>
              <strong style="font-family: Helvetica; color: #343B49;">Age:</strong>  ${c.age}</br>
              <strong style="font-family: Helvetica; color: #343B49;">Duration:</strong>  ${c.duration}</a><br>
              <strong style="font-family: Helvetica; color: #343B49;">Error Details:</strong>
              <div style="font-family: courier;font-size: 10px;text-align:left;overflow: auto;height: 4em;">${c.errorDetails}</div>
              <strong style="font-family: Helvetica; color: #343B49;">Error StackTrace:</strong>
              <pre style="font-family: courier;font-size: 10px;text-align:left;overflow: auto;height: 20em;">${c.errorStackTrace}</pre>
              <hr>
              </td>
            </tr>
          <%}%>
          </table>
        </td>
      </tr>
      <!--TABLE_TEST_ERRORS-->
      <tr>
        <td colspan="7" style="height: 32px;${ log != null ? '' : 'display: none;' }"></td>
      </tr>
      <!--TABLE_LOG-->
      <tr style="background-color: #F4F4F4;${ log != null ? '' : 'display: none;' }">
        <td colspan="6" style="padding-left: 24px;font-family: Helvetica; font-size: 10px; color: #343B49; letter-spacing: 2px; line-height: 16px; margin-bottom: 0; text-transform: uppercase;">
          Log
          <hr>
        </td>
        <td colspan="1" style="background-color: #F4F4F4;"></td>
      </tr>
      <tr style="${ log != null ? '' : 'display: none;' }">
        <td colspan="7" style="padding: 0 24px 0 24px;border-collapse: collapse;background-color: #F4F4F4;">
          <pre style="font-family: courier;font-size: 10px;text-align:left;overflow: auto;">${log}</pre>
        </td>
      </tr>
      <!--TABLE_LOG-->
    </tbody>

    <tfoot style="background-color: #F4F4F4;">
      <tr>
        <td colspan="7" style="padding: 0 24px 0 24px;font-family: Helvetica; font-size: 10px; color: #AEAEAE; letter-spacing: 2px; line-height: 16px; margin-bottom: 0; text-transform: uppercase;">
          Observability
        </td>
      </tr>
      <tr>
        <td colspan="1" style="padding: 0 24px 0 24px;font-family: Helvetica; color: #AEAEAE; letter-spacing: 1px; font-weight: normal; font-size: 12px; line-height: 20px; margin: 8px 0;">
          Build <strong style="font-family: Helvetica; color: #343B49;">Resources</strong>
        </td>
        <td colspan="6" style="text-align: right;padding-right: 67px;">
          <ul style="list-style-type: none; margin: 0; padding: 0;">
            <li style="display: inline; letter-spacing: 1px; font-weight: 700; font-size: 12px; line-height: 20px; font-family: Helvetica; padding-right: 24px;">
              <a href="${jobUrl}?page=pipeline" style="text-decoration: none; color: #343B49;">Pipeline</a>
            </li>
            <li style="display: inline; letter-spacing: 1px; font-weight: 700; font-size: 12px; line-height: 20px; font-family: Helvetica;">
              <a href="${statsUrl}" style="text-decoration: none; color: #343B49;">Stats</a>
            </li>
          </ul>
        </td>
      </tr>
      <tr>
        <td colspan="7" style="margin: 6px;">
        </td>
      </tr>
    </tfoot>
  </table>
</body>

</html>
