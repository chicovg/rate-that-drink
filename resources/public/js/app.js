var beerTable = document.querySelector('#beers');

if (beerTable) {
    var beerRows = beerTable.getElementsByTagName('tr');

    for (row of beerRows) {
        if (row.dataset.beerId) {
            var link = "/user/beers/edit/" + row.dataset.beerId;

            row.onclick = function() {
                window.location = link;
            }
        }
    }
}

var ratings = document.querySelectorAll('.rating');
var total = document.querySelector('.total');

function onRatingChange() {
    let totalValue = 0;
    for (rating of ratings) {
        var value = rating.value;
        if (value) {
            totalValue += new Number(rating.value);
        }
    }
    total.textContent = totalValue;
}

ratings.forEach(function(e) {
    e.addEventListener('change', onRatingChange);
});

$('.accordion')
  .accordion({
    selector: {
      trigger: '.title .icon'
    }
  });

$('table').tablesort();
