(function (document, $, Coral) {
 
  let $doc = $(document);
  $doc.on("foundation-contentloaded", function (e) {
    $(".foundation-form", e.target).each(function (i, element) {
      Coral.commons.ready(element, function (component) {
        $(component).on("change", "[name='./apir']", function (event) {
          let selectedValue = event.target.value;

          if (!selectedValue) {
            return;
          }

          // Construct the API URL dynamically
          let apiUrl = new URL(location.href).searchParams.get('item') ? 
                       new URL(location.href).searchParams.get('item') : 
                       window.location.pathname.split('html')[1];
          apiUrl += '.jsonDataDropdown.json';
          
          $.ajax({
              url: apiUrl,
              type: 'GET',
              data: { apir: selectedValue },
              dataType: 'json',
              success: function(responseData) {
                  console.log(responseData);
                  let marketingName = responseData.marketingName || '';

                  $('[name="./marketingName"]').prop("value", marketingName);

                  if (marketingName) {
                    document.querySelectorAll("[type='submit']")[0].removeAttribute('disabled');
                  } else {
                    document.querySelectorAll("[type='submit']")[0].setAttribute('disabled', 'disabled');
                  }

                  $("[name='./marketingName']").attr("readonly", true);
              },
              error: function(error) {
                  console.error('Error:', error);
              }
          });
        });

        $("[name='./apir']").trigger("change");
      });
    });
  });
})(document, Granite.$, Coral);
