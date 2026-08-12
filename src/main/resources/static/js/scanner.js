(function () {
  document.querySelectorAll(".group-row").forEach(function (row) {
    row.addEventListener("click", function () {
      var id = row.getAttribute("data-group");
      document.querySelectorAll('[data-rows="' + id + '"]').forEach(function (el) {
        el.classList.toggle("hidden");
      });
      var toggle = row.querySelector(".toggle");
      if (toggle) {
        toggle.textContent = toggle.textContent === "▼" ? "▲" : "▼";
      }
    });
  });

  var table = document.getElementById("strangle-table");
  if (table) {
    table.querySelectorAll("th[data-sort]").forEach(function (th) {
      th.addEventListener("click", function () {
        sortTable(table, th.cellIndex, th.getAttribute("data-sort"));
      });
    });
    table.querySelectorAll("tbody tr").forEach(function (row) {
      row.addEventListener("click", function () {
        table.querySelectorAll("tbody tr").forEach(function (r) { r.classList.remove("active"); });
        row.classList.add("active");
        var detail = document.getElementById("detail-" + row.getAttribute("data-id"));
        document.querySelectorAll(".candidate-detail").forEach(function (el) {
          el.classList.add("hidden");
        });
        if (detail) {
          detail.classList.remove("hidden");
        }
      });
    });
  }

  function sortTable(tbl, col, type) {
    var tbody = tbl.tBodies[0];
    var rows = Array.from(tbody.rows);
    var dir = tbl.getAttribute("data-dir") === "asc" ? "desc" : "asc";
    tbl.setAttribute("data-dir", dir);
    rows.sort(function (a, b) {
      var av = a.cells[col].getAttribute("data-value") || a.cells[col].innerText;
      var bv = b.cells[col].getAttribute("data-value") || b.cells[col].innerText;
      if (type === "num") {
        av = parseFloat(av);
        bv = parseFloat(bv);
        return dir === "asc" ? av - bv : bv - av;
      }
      return dir === "asc" ? av.localeCompare(bv) : bv.localeCompare(av);
    });
    rows.forEach(function (row) { tbody.appendChild(row); });
  }
})();
