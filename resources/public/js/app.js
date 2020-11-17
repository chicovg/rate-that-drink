var beerRows = document.querySelectorAll('#beers > tbody > tr');

beerRows.forEach(function(row) {
    var beerId = row.dataset.beerId;
    if (row.children && beerId) {
        for (var c = 0; c < row.children.length; c++) {
            var cell = row.children.item(c);
            cell.addEventListener('click', function() {
                window.location = '/user/beers/edit/' + beerId;
            });
        }
    }
});

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

$('.accordion').accordion({
    selector: {
        trigger: '.title .icon',
    },
});

$('table').tablesort();
