/**
 * 게시판 공통 JavaScript
 */

(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    initReplyFormFocus();
    initPostFormAjax();
    initPostDeleteAjax();
    initCommentAjax();
  });

  function initReplyFormFocus() {
    document.querySelectorAll('[id^="reply-form-"]').forEach(function (el) {
      el.addEventListener('shown.bs.collapse', function () {
        var input = this.querySelector('input[name="content"]');
        if (input) input.focus();
      });
    });
  }

  /** 게시글 수정 - Ajax PUT */
  function initPostFormAjax() {
    var form = document.getElementById('post-form');
    if (!form || !form.dataset.postId) return;
    var boardCode = form.dataset.boardCode || 'GENERAL';
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      var postId = form.dataset.postId;
      var data = {
        title: form.querySelector('#title').value,
        content: form.querySelector('#content').value
      };
      if (boardCode === 'VENUE') {
        data.location = form.querySelector('#location') ? form.querySelector('#location').value : null;
        data.mealPrice = form.querySelector('#mealPrice') ? parseInt(form.querySelector('#mealPrice').value, 10) || null : null;
        data.guaranteeMin = form.querySelector('#guaranteeMin') ? parseInt(form.querySelector('#guaranteeMin').value, 10) || null : null;
        data.rentalFee = form.querySelector('#rentalFee') ? parseInt(form.querySelector('#rentalFee').value, 10) || null : null;
        data.etcFee = form.querySelector('#etcFee') ? parseInt(form.querySelector('#etcFee').value, 10) || null : null;
      }
      var btn = document.getElementById('post-submit-btn');
      if (btn) btn.disabled = true;
      BoardAjax.put('/boards/' + boardCode + '/posts/' + postId, data)
        .then(function (r) { return BoardAjax.handleResponse(r); })
        .then(function (body) {
          if (body.success) window.location.href = '/boards/' + boardCode + '/posts/' + body.id;
        })
        .catch(function (err) {
          if (btn) btn.disabled = false;
          alert(err.body && err.body.message ? err.body.message : '수정에 실패했습니다.');
        });
    });
  }

  /** 게시글 삭제 - Ajax DELETE */
  function initPostDeleteAjax() {
    var btn = document.getElementById('post-delete-btn');
    if (!btn) return;
    var boardCode = btn.dataset.boardCode || 'GENERAL';
    btn.addEventListener('click', function () {
      if (!confirm('정말 삭제하시겠습니까?')) return;
      var postId = btn.dataset.postId;
      btn.disabled = true;
      BoardAjax.delete('/boards/' + boardCode + '/posts/' + postId)
        .then(function (r) { return BoardAjax.handleResponse(r); })
        .then(function () { window.location.href = '/boards/' + boardCode + '/posts'; })
        .catch(function () {
          btn.disabled = false;
          alert('삭제에 실패했습니다.');
        });
    });
  }

  /** 댓글 수정/삭제 - Ajax PUT, DELETE */
  function initCommentAjax() {
    document.body.addEventListener('submit', function (e) {
      var form = e.target;
      if (!form.classList.contains('comment-edit-form')) return;
      e.preventDefault();
      var boardCode = form.dataset.boardCode || 'GENERAL';
      var postId = form.dataset.postId;
      var commentId = form.dataset.commentId;
      var content = (form.querySelector('input[name="content"]') || form.querySelector('textarea[name="content"]')).value;
      var wrap = form.closest('.comment-edit-form-wrap');
      BoardAjax.put('/boards/' + boardCode + '/posts/' + postId + '/comments/' + commentId, { content: content })
        .then(function (r) { return BoardAjax.handleResponse(r); })
        .then(function (body) {
          if (body.success) {
            var item = form.closest('.comment-item');
            var depth = item.dataset.depth || '0';
            var isReply = parseInt(depth, 10) > 0;
            var viewHtml = '<div class="comment-view-wrap"><p class="comment-content mb-1 mt-1' + (isReply ? ' small' : '') + '">' + escapeHtml(body.content) + '</p>' +
              '<div class="small d-flex align-items-center flex-wrap comment-actions" style="gap: 0.25rem 0.75rem;">' +
              '<a href="/boards/' + boardCode + '/posts/' + postId + '?editComment=' + commentId + '" class="btn btn-link btn-sm p-0 m-0 text-primary text-decoration-none border-0 comment-edit-link" style="font-size: inherit; line-height: 1.5;">수정</a> ' +
              '<button type="button" class="btn btn-link btn-sm p-0 m-0 text-danger text-decoration-none border-0 comment-delete-btn" style="font-size: inherit; line-height: 1.5;" data-board-code="' + boardCode + '" data-post-id="' + postId + '" data-comment-id="' + commentId + '">삭제</button>' +
              '</div></div>';
            wrap.outerHTML = viewHtml;
          }
        })
        .catch(function (err) {
          alert(err.body && err.body.message ? err.body.message : '수정에 실패했습니다.');
        });
    });

    document.body.addEventListener('click', function (e) {
      var btn = e.target.closest('.comment-delete-btn');
      if (!btn) return;
      e.preventDefault();
      if (!confirm('댓글을 삭제하시겠습니까?')) return;
      var boardCode = btn.dataset.boardCode || 'GENERAL';
      var postId = btn.dataset.postId;
      var commentId = btn.dataset.commentId;
      var item = btn.closest('.comment-item');
      btn.disabled = true;
      BoardAjax.delete('/boards/' + boardCode + '/posts/' + postId + '/comments/' + commentId)
        .then(function (r) { return BoardAjax.handleResponse(r); })
        .then(function () {
          var depth = item.dataset.depth || '0';
          var isReply = parseInt(depth, 10) > 0;
          var deletedText = isReply ? '삭제된 답글입니다' : '삭제된 댓글입니다';
          var inner = item.querySelector('.comment-view-wrap, .comment-edit-form-wrap');
          if (inner) {
            inner.outerHTML = '<div class="mb-1 mt-1"><p class="' + (isReply ? 'small text-muted fst-italic mb-0' : 'text-muted fst-italic mb-0') + ' mb-1">' + deletedText + '</p></div>';
          }
          var replyBtnParent = item.querySelector('button[data-bs-target="#reply-form-' + commentId + '"]');
          if (replyBtnParent) replyBtnParent.closest('.mt-1').remove();
        })
        .catch(function () {
          btn.disabled = false;
          alert('삭제에 실패했습니다.');
        });
    });
  }

  function escapeHtml(str) {
    var div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }
})();
