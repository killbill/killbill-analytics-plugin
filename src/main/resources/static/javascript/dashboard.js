$(document).ready(function() {
  $('#start-date').datepicker({
      autoclose: true,
      todayHighlight: true
  });

  $('#end-date').datepicker({
      autoclose: true,
      todayHighlight: true
  });

  $("#refresh-graphs").click(function() {
    var url = $(location).attr('href');

    var startDatepicker = $('#start-date').data('datepicker');
    if (startDatepicker && startDatepicker.dates.length > 0) {
      var startDate = startDatepicker.getDate();
      var startDateString = startDate.getFullYear() + '-' + (startDate.getMonth() + 1) + '-' + startDate.getDate();
      url = updateURLParameter(url, 'startDate', startDateString);
    }

    var endDatepicker = $('#end-date').data('datepicker');
    if (endDatepicker && endDatepicker.dates.length > 0) {
      var endDate = endDatepicker.getDate();
      var endDateString = endDate.getFullYear() + '-' + (endDate.getMonth() + 1) + '-' + endDate.getDate();
      url = updateURLParameter(url, 'endDate', endDateString);
    }

    $(location).attr('href', url);
  });
});

/**
 * http://stackoverflow.com/a/10997390/11236
 */
function updateURLParameter(url, param, paramVal) {
    var TheAnchor = null;
    var newAdditionalURL = "";
    var tempArray = url.split("?");
    var baseURL = tempArray[0];
    var additionalURL = tempArray[1];
    var temp = "";

    if (additionalURL) {
        var tmpAnchor = additionalURL.split("#");
        var TheParams = tmpAnchor[0];
            TheAnchor = tmpAnchor[1];
        if (TheAnchor) {
            additionalURL = TheParams;
        }

        tempArray = additionalURL.split("&");

        for (i=0; i<tempArray.length; i++) {
            if(tempArray[i].split('=')[0] != param){
                newAdditionalURL += temp + tempArray[i];
                temp = "&";
            }
        }
    }
    else {
        var tmpAnchor = baseURL.split("#");
        var TheParams = tmpAnchor[0];
            TheAnchor  = tmpAnchor[1];

        if (TheParams) {
            baseURL = TheParams;
        }
    }

    if (TheAnchor) {
        paramVal += "#" + TheAnchor;
    }

    var rows_txt = temp + "" + param + "=" + paramVal;
    return baseURL + "?" + newAdditionalURL + rows_txt;
}