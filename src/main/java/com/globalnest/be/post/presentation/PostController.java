package com.globalnest.be.post.presentation;

import com.globalnest.be.global.dto.ResponseTemplate;
import com.globalnest.be.global.util.translation.TranslationConverter;
import com.globalnest.be.oauth.dto.CustomOAuth2User;
import com.globalnest.be.post.application.PostService;
import com.globalnest.be.post.application.type.SortType;
import com.globalnest.be.post.dto.request.PostUploadRequest;
import com.globalnest.be.post.dto.response.PostDetailResponse;
import com.globalnest.be.post.dto.response.PostResponseList;
import com.globalnest.be.user.domain.type.Part;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Post", description = "게시글 관련 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final TranslationConverter translationConverter;

    @Operation(summary = "게시글 리스트 조회", description = "게시글 리스트를 조회합니다<br>"
        + "페이지 번호, 페이지 크기, 정렬 방식을 입력받아 게시글 리스트를 반환합니다<br>"
        + "page는 0번부터 시작")
    @GetMapping
    public ResponseEntity<?> getNearbyLecturePlaces(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(required = false) SortType sortType,
        @RequestParam(required = false) Part part,
        @AuthenticationPrincipal CustomOAuth2User user
    ) {
        PostResponseList postResponseList =
            postService.findPostResponseList(user.getUserId(), page, size, sortType, part);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(translationConverter.getChatResponse(postResponseList, user.getLanguage()));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다. 마지막 확인 댓글 ID를 기준으로 댓글을 조회합니다<br>"
        + "이후 댓글은 /comments/{postId} API를 통해 추가 조회합니다")
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetail(
        @PathVariable Long postId,
        @RequestParam(defaultValue = "0") Long lastCommentId,
        @RequestParam(defaultValue = "5") int size,
        @AuthenticationPrincipal CustomOAuth2User user
    ) {
        PostDetailResponse postDetailResponse =
            postService.findPostDetailResponse(user.getUserId(), postId, lastCommentId, size);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(translationConverter.getChatResponse(postDetailResponse, user.getLanguage()));
    }

    @Operation(summary = "게시글 업로드", description = "게시글을 업로드합니다<br>"
        + "Tag는 자율로 입력")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> uploadPost(
        @RequestPart PostUploadRequest postUploadRequest,
        @RequestPart(required = false) MultipartFile file,
        @AuthenticationPrincipal CustomOAuth2User user
    ) {
        postService.uploadPost(user.getUserId(), postUploadRequest, file);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(null);
    }

    @Operation(summary = "게시글 좋아요 저장/삭제", description = "게시글 좋아요 저장/삭제")
    @GetMapping("/like/{postId}")
    public ResponseEntity<?> saveBookMark(
        @PathVariable Long postId,
        @AuthenticationPrincipal CustomOAuth2User user
    ) {
        String result = postService.likePost(user.getUserId(), postId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(translationConverter.getChatResponse(result, user.getLanguage()));
    }
}
