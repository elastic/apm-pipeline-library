<!DOCTYPE html>
<html lang="en">

<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta charset="UTF-8">
  <title>Email</title>
</head>

<body style="margin: 0; padding: 0;">
  <table logopacing="0" style="width: 90%;border-collapse: collapse;">
    <thead style="background-color: #FFFFFF;">
      <tr>
        <td colspan="7" style="height: 32px;"></td>
      </tr>
      <tr>
        <td rowspan="2" colspan="1" style="padding-left: 10px;"></td>
        <td rowspan="2" colspan="2" style="padding: 0 0 10px 0;width: 156px;text-align: center;">
          <img alt="logo" src="https://avatars0.githubusercontent.com/u/6764390?s=100&v=4">
        </td>
        <td colspan="3" style="font-family: Helvetica; font-size: 12px; color: #343B49; letter-spacing: 2px; line-height: 16px; text-transform: uppercase;">
          Observability-robots
        </td>
      </tr>
      <tr>
        <td colspan="4" style="display:inline;letter-spacing:1px;font-weight:700;font-size:24px;line-height:30px;font-family:Lato-Bold,Helvetica;padding-right:24px">
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
      <!--TABLE1-->
      <tr>
        <td rowspan="3" colspan="1"></td>
        <td rowspan="3" colspan="2" style="text-align: center;">
          <img alt="jenkins" src="https://avatars0.githubusercontent.com/u/107424?s=100&v=4">
        </td>
        <td colspan="2" style="vertical-align: bottom;font-family: Georgia; font-weight: bold; font-size: 16px; color: #343B49; letter-spacing: 1px; line-height: 16px;">
          Jenkins
        </td>
        <td rowspan="3" colspan="2" style="padding-right: 24px;">
          <table logopacing="0" cellpadding="0" width="142px" style="text-align: center;">
            <tr>
              <td width="64px" height="142px">
                <a href="${jenkinsUrl}" style="text-decoration: none;border-radius: 50%;background-color: #ffffff;display: inline-block;height: 72px;width: 72px;line-height: 76px;text-align: center">
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
        <td colspan="2"></td>
        <td colspan="5" style="vertical-align: middle; font-family: Helvetica; font-size: 12px; color: #343B49; letter-spacing: 1px;">
          Build Cause: ${build?.causes?.shortDescription}
        </td>
      </tr>
      <tr>
        <td colspan="2"></td>
        <td colspan="5" style="vertical-align: middle; font-family: Helvetica; font-size: 12px; color: #343B49; letter-spacing: 1px;">
          Start Time: ${build?.startTime}
        </td>
      </tr>
      <tr>
        <td colspan="2"></td>
        <td colspan="5" style="vertical-align: middle; font-family: Helvetica; font-size: 12px; color: #343B49; letter-spacing: 1px;">
          Duration: ${Math.round(build?.durationInMillis/1000/60)} min ${Math.round(build?.durationInMillis/1000)%60} sec - ${build?.durationInMillis}
        </td>
      </tr>
      <!--TABLE1-->
      <tr>
        <td colspan="7" style="height: 32px;"></td>
      </tr>
      <!--TABLE2-->
      <tr style="background-color: #F4F4F4;${ testsSummary?.total != null ? '' : 'display: none;' }">
        <td colspan="1" style="background-color: #F4F4F4;"></td>
        <td colspan="6" style="padding-top: 10px;vertical-align: bottom; font-family: Helvetica; font-size: 12px; color: #343B49; letter-spacing: 2px; line-height: 16px; text-transform: uppercase;">
          Test Results
          <hr>
        </td>
      </tr>
      <tr style="${ testsSummary?.total != null ? '' : 'display: none;' }">
        <td colspan="2" style="background-color: #F4F4F4;"></td>
        <td colspan="5" style="padding: 0 24px 0 0;border-collapse: collapse;">
          Failed: ${testsSummary.failed}<br>
          Passed: ${testsSummary.passed}<br>
          Skipped: ${testsSummary.skipped}<br>
          Total: ${testsSummary.total}<br>
        </td>
      </tr>
      <!--TABLE2-->
      <tr>
        <td colspan="7" style="height: 32px;${ changeSet?.size() != 0 ? '' : 'display: none;' }"></td>
      </tr>
      <!--TABLE3-->
      <tr style="background-color: #F4F4F4;${ changeSet?.size() != 0 ? '' : 'display: none;' }">
        <td colspan="1" style="background-color: #F4F4F4;"></td>
        <td colspan="6" style="font-family: Helvetica; font-size: 10px; color: #343B49; letter-spacing: 2px; line-height: 16px; margin-bottom: 0; text-transform: uppercase;">
          Changes
          <hr>
        </td>
      </tr>
      <tr>
        <td colspan="1" style="background-color: #F4F4F4;"></td>
        <td colspan="6" style="padding: 0 24px 0 0;border-collapse: collapse;background-color: #F4F4F4;">
          <table logopacing="0" cellpadding="0" style="text-align: center;">
          <% changeSet.each{ c -> %>
            <tr>
              <td>
              Author: ${c.author.id}<br>
              Full Name: ${c.author.fullName}<br>
              email: ${c.author.email}</br>
              Commit: <a href="${c.url}">${c.commitId}</a><br>
              Message: ${c.msg}<br>
              Date: ${c.timestamp}<br>
              </td>
            </tr>
          <%}%>
          </table>
        </td>
      </tr>
      <!--TABLE3-->
      <tr>
        <td colspan="7" style="height: 32px;${ log != null ? '' : 'display: none;' }"></td>
      </tr>
      <!--TABLE4-->
      <tr style="background-color: #F4F4F4;${ log != null ? '' : 'display: none;' }">
        <td colspan="1" style="background-color: #F4F4F4;"></td>
        <td colspan="6" style="font-family: Helvetica; font-size: 10px; color: #343B49; letter-spacing: 2px; line-height: 16px; margin-bottom: 0; text-transform: uppercase;">
          Log
          <hr>
        </td>
      </tr>
      <tr style="${ log != null ? '' : 'display: none;' }">
        <td colspan="7" style="padding: 0 24px 0 24px;border-collapse: collapse;background-color: #F4F4F4;">
          <pre style="font-family: courier; font-size: 10px;">${log}</pre>
        </td>
      </tr>
      <!--TABLE4-->
    </tbody>

    <tfoot style="background-color: #F4F4F4;">
      <tr>
        <td colspan="7" style="font-family: Helvetica; font-size: 10px; color: #AEAEAE; letter-spacing: 2px; line-height: 16px; margin-bottom: 0; text-transform: uppercase;">
          Observability
        </td>
      </tr>
      <tr>
        <td colspan="1" style="font-family: Helvetica; color: #AEAEAE; letter-spacing: 1px; font-weight: normal; font-size: 12px; line-height: 20px; margin: 8px 0;">
          Build <strong style="font-family: Helvetica; color: #343B49;">Resources</strong>
        </td>
        <td colspan="6" style="text-align: right;padding-right: 67px;">
          <ul style="list-style-type: none; margin: 0; padding: 0;">
            <li style="display: inline; letter-spacing: 1px; font-weight: 700; font-size: 12px; line-height: 20px; font-family: Helvetica; padding-right: 24px;">
              <a href="${jenkinsUrl}?page=pipeline" style="text-decoration: none; color: #343B49;">Pipeline</a>
            </li>
            <li style="display: inline; letter-spacing: 1px; font-weight: 700; font-size: 12px; line-height: 20px; font-family: Helvetica;">
              <a href="${statsUrl}?page=changes" style="text-decoration: none; color: #343B49;">Stats</a>
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
