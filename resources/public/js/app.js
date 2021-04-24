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

function updateDrinkTotal() {
    var appearanceVal = $('[name=appearance]').val();
    var smellVal = $('[name=smell]').val();
    var tasteVal = $('[name=taste]').val();
    var sum = new Number(appearanceVal) + new Number(smellVal) + new Number(tasteVal) * 3;
    var averaged = sum / 5;

    $('[name=total]').text(averaged.toFixed(1));
}

$(document).ready(function() {
    $('#drinks').DataTable({
        responsive: {
            details: false,
        },
        columnDefs: [
            { responsivePriority: 1, targets: 0 },
            { responsivePriority: 2, targets: -1 },
            { responsivePriority: 3, targets: 1 },
        ]
    });

    $('#drinks tbody').on('click', 'tr', function() {
        drinkId = $(this).data('drinkId');
        window.location = '/user/drinks/edit/' + drinkId;
    });

    $('drink-rating').ready(updateDrinkTotal);

    $('.drink-rating').on('change', updateDrinkTotal);
})
