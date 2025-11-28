$(document).ready(function() {
  // Configuração padrão para todos os DataTables
  $.extend(true, $.fn.dataTable.defaults, {
    pageLength: 10,
    searching: true,
    ordering: true,
    order: [],
    responsive: true,
    dom: '<"row"<"col-sm-6"B><"col-sm-6"f>>' +  // Botões ESQUERDA + Filtro DIREITA
         '<"row"<"col-sm-12"tr>>' +              // Tabela
         '<"row"<"col-sm-12 col-md-6"i><"col-sm-12 col-md-6"p>>', // Info e paginação
    buttons: [
      {
        extend: 'excelHtml5',
        text: '<i class="fas fa-file-excel me-1"></i>Excel',
        titleAttr: 'Exportar para Excel',
        className: 'btn btn-success btn-sm',
        exportOptions: {
          columns: ':visible'
        }
      },
      {
        extend: 'pdfHtml5',
        text: '<i class="fas fa-file-pdf me-1"></i>PDF',
        titleAttr: 'Exportar para PDF',
        className: 'btn btn-danger btn-sm',
        orientation: 'landscape',
        pageSize: 'A4',
        exportOptions: {
          columns: ':visible'
        }
      },
      {
        extend: 'print',
        text: '<i class="fas fa-print me-1"></i>Imprimir',
        titleAttr: 'Imprimir tabela',
        className: 'btn btn-info btn-sm',
        exportOptions: {
          columns: ':visible'
        }
      }
    ],
    language: {
      emptyTable: "Nenhum registro encontrado. ", // Mensagem quando a tabela está vazia
      search: "Buscar:",
      searchPlaceholder: "Digite para buscar...",
      zeroRecords: "Nenhum registro encontrado", // Mensagem quando busca não retorna resultados
      info: "Mostrando _START_ a _END_ de _TOTAL_ registros",
      infoEmpty: "Mostrando 0 a 0 de 0 registros",
      infoFiltered: "(filtrado de _MAX_ registros no total)",
      paginate: {
        first: "Primeiro",
        last: "Último",
        next: "Próximo",
        previous: "Anterior"
      }
    },
    // Remove o seletor de quantidade da interface
    lengthChange: false,
    initComplete: function(settings, json) {
      // Adiciona classe personalizada aos botões
      $('.dt-buttons .btn').addClass('me-1 mb-2');
      $('.dt-buttons').addClass('mb-3');
      
      // Opcional: Alinhar o campo de busca à direita
      $('.dataTables_filter').addClass('text-end');
    }
  });
});