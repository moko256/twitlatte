/*
 * Copyright 2015-2018 The twitlatte authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twitlatte

import android.os.Build
import android.text.Layout
import android.text.TextUtils
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.moko256.twitlatte.entity.Repeat
import com.github.moko256.twitlatte.entity.Status
import com.github.moko256.twitlatte.entity.User
import com.github.moko256.twitlatte.entity.Visibility
import com.github.moko256.twitlatte.glide.GlideRequests
import com.github.moko256.twitlatte.repository.KEY_TIMELINE_IMAGE_LOAD_MODE
import com.github.moko256.twitlatte.text.TwitterStringUtils
import com.github.moko256.twitlatte.view.EmojiToTextViewSetter
import com.github.moko256.twitlatte.widget.CheckableImageView
import com.github.moko256.twitlatte.widget.TweetImageTableView
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by moko256 on 2018/09/03.
 *
 * @author moko256
 */
class StatusViewBinder(private val glideRequests: GlideRequests, private val viewGroup: ViewGroup) {
    private val disposable = CompositeDisposable()

    private var hasStatus = false

    private var contextEmojiSetter: EmojiToTextViewSetter? = null
    private var spoilerTextEmojiSetter: EmojiToTextViewSetter? = null
    private var userNameEmojiSetter: EmojiToTextViewSetter? = null

    val repeatUserName: TextView = viewGroup.findViewById(R.id.tweet_retweet_user_name)
    val repeatTimeStamp: TextView = viewGroup.findViewById(R.id.tweet_retweet_time_stamp_text)
    val userImage: ImageView = viewGroup.findViewById(R.id.tweet_icon)
    val replyUserName: TextView = viewGroup.findViewById(R.id.tweet_reply_user_name)
    val userName: TextView = viewGroup.findViewById(R.id.tweet_user_name)
    val userId: TextView = viewGroup.findViewById(R.id.tweet_user_id)
    val tweetSpoilerText: TextView = viewGroup.findViewById(R.id.tweet_spoiler)
    val contentOpenerToggle: CheckableImageView = viewGroup.findViewById(R.id.tweet_spoiler_opener)
    val tweetContext: TextView = viewGroup.findViewById(R.id.tweet_content)
    val timeStampText: TextView = viewGroup.findViewById(R.id.tweet_time_stamp_text)
    val quoteTweetLayout: RelativeLayout = viewGroup.findViewById(R.id.tweet_quote_tweet)
    val quoteTweetImages: TweetImageTableView = viewGroup.findViewById(R.id.tweet_quote_images)
    val quoteTweetUserName: TextView = viewGroup.findViewById(R.id.tweet_quote_tweet_user_name)
    val quoteTweetUserId: TextView = viewGroup.findViewById(R.id.tweet_quote_tweet_user_id)
    val quoteTweetContext: TextView = viewGroup.findViewById(R.id.tweet_quote_tweet_content)
    val imageTableView: TweetImageTableView = viewGroup.findViewById(R.id.tweet_image_container)
    val likeButton: CheckableImageView = viewGroup.findViewById(R.id.tweet_content_like_button)
    val repeatButton: CheckableImageView = viewGroup.findViewById(R.id.tweet_content_retweet_button)
    val replyButton: ImageButton = viewGroup.findViewById(R.id.tweet_content_reply_button)
    val likeCount: TextView = viewGroup.findViewById(R.id.tweet_content_like_count)
    val repeatCount: TextView = viewGroup.findViewById(R.id.tweet_content_retweet_count)
    val repliesCount: TextView = viewGroup.findViewById(R.id.tweet_content_replies_count)

    init {
        contentOpenerToggle.onCheckedChangeListener = { _, isChecked ->
            tweetContext.visibility = if (isChecked) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        tweetContext.movementMethod = LinkMovementMethod.getInstance()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tweetContext.breakStrategy = Layout.BREAK_STRATEGY_SIMPLE
            quoteTweetContext.breakStrategy = Layout.BREAK_STRATEGY_SIMPLE
        }
    }

    fun setStatus(
            repeatedUser: User?,
            repeated: Repeat?,
            user: User?,
            status: Status,
            quotedStatusUser: User?,
            quotedStatus: Status?
    ) {
        if (hasStatus) {
            clear()
        }

        updateView(repeatedUser, repeated, user, status, quotedStatusUser, quotedStatus)
        hasStatus = true
    }

    private fun updateView(
            repeatedUser: User?,
            repeated: Repeat?,
            user: User?,
            status: Status,
            quotedStatusUser: User?,
            quotedStatus: Status?
    ) {

        if (repeatedUser != null) {
            if (repeatUserName.visibility != View.VISIBLE) {
                repeatUserName.visibility = View.VISIBLE
            }
            repeatUserName.text = viewGroup.context.getString(
                    TwitterStringUtils.getRepeatedByStringRes(GlobalApplication.clientType),
                    repeatedUser.name,
                    TwitterStringUtils.plusAtMark(repeatedUser.screenName)
            )
        } else {
            if (repeatUserName.visibility != View.GONE) {
                repeatUserName.visibility = View.GONE
            }
        }

        if (repeated != null) {
            if (repeatTimeStamp.visibility != View.VISIBLE) {
                repeatTimeStamp.visibility = View.VISIBLE
            }
            repeatTimeStamp.text = DateUtils.getRelativeTimeSpanString(
                    status.createdAt.time,
                    System.currentTimeMillis(),
                    0
            )
        } else {
            if (repeatTimeStamp.visibility != View.GONE) {
                repeatTimeStamp.visibility = View.GONE
            }
        }

        val isReply = status.inReplyToScreenName != null
        if (isReply) {
            if (replyUserName.visibility != View.VISIBLE) {
                replyUserName.visibility = View.VISIBLE
            }

            replyUserName.text= if (status.inReplyToScreenName?.isEmpty() == true) {
                viewGroup.context.getString(
                        if (status.inReplyToStatusId != -1L) {
                            R.string.reply
                        } else {
                            R.string.mention
                        }
                )
            } else {
                viewGroup.context.getString(
                        if (status.inReplyToStatusId != -1L) {
                            R.string.reply_to
                        } else {
                            R.string.mention_to
                        },
                        TwitterStringUtils.plusAtMark(status.inReplyToScreenName)
                )
            }
        } else {
            if (replyUserName.visibility != View.GONE) {
                replyUserName.visibility = View.GONE
            }
        }

        val timelineImageLoadMode = GlobalApplication.preferenceRepository.getString(KEY_TIMELINE_IMAGE_LOAD_MODE, "normal")
        if (timelineImageLoadMode != "none") {
            glideRequests
                    .load(
                            if (timelineImageLoadMode == "normal")
                                user?.get400x400ProfileImageURLHttps()
                            else
                                user?.getMiniProfileImageURLHttps()
                    )
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(userImage)
        } else {
            userImage.setImageResource(R.drawable.border_frame_round)
        }

        val userNameText = TwitterStringUtils.plusUserMarks(
                user?.name,
                userName,
                user?.isProtected == true,
                user?.isVerified == true
        )
        userName.text = userNameText
        val userNameEmojis = user?.emojis
        if (userNameEmojis != null) {
            if (userNameEmojiSetter == null) {
                userNameEmojiSetter = EmojiToTextViewSetter(glideRequests, userName)
            }
            val set = userNameEmojiSetter!!.set(userNameText, userNameEmojis)
            if (set != null) {
                disposable.addAll(*set)
            }
        }
        userId.text = TwitterStringUtils.plusAtMark(user?.screenName)

        val linkedSequence = TwitterStringUtils.getLinkedSequence(viewGroup.context, status.text, status.urls)

        tweetContext.text = linkedSequence

        if (!TextUtils.isEmpty(linkedSequence)) {
            if (tweetContext.visibility != View.VISIBLE) {
                tweetContext.visibility = View.VISIBLE
            }

            if (status.emojis != null) {
                if (contextEmojiSetter == null) {
                    contextEmojiSetter = EmojiToTextViewSetter(glideRequests, tweetContext)
                }
                val set = contextEmojiSetter!!.set(linkedSequence, status.emojis)
                if (set != null) {
                    disposable.addAll(*set)
                }
            }
        } else {
            if (tweetContext.visibility != View.GONE) {
                tweetContext.visibility = View.GONE
            }
        }

        tweetSpoilerText.text = status.spoilerText
        if (status.spoilerText == null) {
            if (tweetSpoilerText.visibility != View.GONE) {
                tweetSpoilerText.visibility = View.GONE
            }
            if (contentOpenerToggle.visibility != View.GONE) {
                contentOpenerToggle.visibility = View.GONE
            }
        } else {
            contentOpenerToggle.isChecked = false
            if (tweetContext.visibility != View.GONE) {
                tweetContext.visibility = View.GONE
            }
            if (tweetSpoilerText.visibility != View.VISIBLE) {
                tweetSpoilerText.visibility = View.VISIBLE
            }
            if (contentOpenerToggle.visibility != View.VISIBLE) {
                contentOpenerToggle.visibility = View.VISIBLE
            }

            if (status.emojis != null) {
                if (spoilerTextEmojiSetter == null) {
                    spoilerTextEmojiSetter = EmojiToTextViewSetter(glideRequests, tweetSpoilerText)
                }
                val set = spoilerTextEmojiSetter!!.set(status.spoilerText, status.emojis)
                if (set != null) {
                    disposable.addAll(*set)
                }
            }
        }

        timeStampText.text = DateUtils.getRelativeTimeSpanString(
                status.createdAt.time,
                System.currentTimeMillis(),
                0
        )

        if (quotedStatus != null && quotedStatusUser != null) {
            if (quoteTweetLayout.visibility != View.VISIBLE) {
                quoteTweetLayout.visibility = View.VISIBLE
            }
            quoteTweetUserName.text = TwitterStringUtils.plusUserMarks(
                    quotedStatusUser.name,
                    quoteTweetUserName,
                    quotedStatusUser.isProtected,
                    quotedStatusUser.isVerified
            )
            if (quotedStatus.medias?.isNotEmpty() == true) {
                if (quoteTweetImages.visibility != View.VISIBLE) {
                    quoteTweetImages.visibility = View.VISIBLE
                }
                quoteTweetImages.setMediaEntities(quotedStatus.medias, quotedStatus.isSensitive)
            } else {
                if (quoteTweetImages.visibility != View.GONE) {
                    quoteTweetImages.visibility = View.GONE
                }
            }
            quoteTweetUserId.text = TwitterStringUtils.plusAtMark(quotedStatusUser.screenName)
            quoteTweetContext.text = quotedStatus.text
        } else {
            if (quoteTweetLayout.visibility != View.GONE) {
                quoteTweetLayout.visibility = View.GONE
            }
        }

        val medias = status.medias

        if (medias?.isNotEmpty() == true) {
            imageTableView.visibility = View.VISIBLE
            imageTableView.setMediaEntities(medias, status.isSensitive)
        } else {
            imageTableView.visibility = View.GONE
        }

        likeButton.isChecked = status.isFavorited

        repeatButton.isChecked = status.isRepeated

        val isRepeatEnabled: Boolean
        val repeatIconResourceId: Int

        if (status.visibility != null) {
            when {
                status.visibility == Visibility.Private.value -> {
                    isRepeatEnabled = false
                    repeatIconResourceId = R.drawable.lock_button_stateful
                }
                status.visibility == Visibility.Direct.value -> {
                    isRepeatEnabled = false
                    repeatIconResourceId = R.drawable.dm_button_stateful
                }
                else -> {
                    isRepeatEnabled = true
                    repeatIconResourceId = R.drawable.repeat_button_stateful
                }
            }
        } else {
            if (user != null && (!user.isProtected || user.id == GlobalApplication.accessToken.userId)) {
                isRepeatEnabled = true
                repeatIconResourceId = R.drawable.repeat_button_stateful
            } else {
                isRepeatEnabled = false
                repeatIconResourceId = R.drawable.lock_button_stateful
            }
        }
        repeatButton.isEnabled = isRepeatEnabled
        repeatButton.setImageDrawable(ContextCompat.getDrawable(viewGroup.context, repeatIconResourceId))

        likeCount.text = if (status.favoriteCount != 0) TwitterStringUtils.convertToSIUnitString(status.favoriteCount) else ""
        repeatCount.text = if (status.repeatCount != 0) TwitterStringUtils.convertToSIUnitString(status.repeatCount) else ""
        repliesCount.text = if (status.repliesCount != 0) TwitterStringUtils.convertToSIUnitString(status.repliesCount) else ""
    }

    fun clear() {
        disposable.clear()
        hasStatus = false
    }

}