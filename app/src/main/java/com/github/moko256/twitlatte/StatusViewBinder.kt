/*
 * Copyright 2015-2019 The twitlatte authors
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

import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.TextUtils
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.moko256.latte.client.base.CLIENT_TYPE_NOTHING
import com.github.moko256.latte.client.base.entity.*
import com.github.moko256.twitlatte.glide.GlideRequests
import com.github.moko256.twitlatte.repository.KEY_TIMELINE_IMAGE_LOAD_MODE
import com.github.moko256.twitlatte.text.TwitterStringUtils
import com.github.moko256.twitlatte.view.EmojiToTextViewSetter
import com.github.moko256.twitlatte.widget.CheckableImageView
import com.github.moko256.twitlatte.widget.ImagesTableView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by moko256 on 2018/09/03.
 *
 * @author moko256
 */
class StatusViewBinder(
        private val accessToken: AccessToken,
        private val glideRequests: GlideRequests,
        private val viewGroup: ViewGroup
) {
    private val disposable = CompositeDisposable()

    private var hasStatus = false

    private var contextEmojiSetter: EmojiToTextViewSetter? = null
    private var spoilerTextEmojiSetter: EmojiToTextViewSetter? = null
    private var userNameEmojiSetter: EmojiToTextViewSetter? = null

    private val units = viewGroup.resources.getString(R.string.num_units).toCharArray()
    private val unitsExponent = viewGroup.resources.getInteger(R.integer.num_unit_exponent)
    private val unitsBack = viewGroup.resources.getInteger(R.integer.num_unit_back)

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
    val additionalContentLayout: RelativeLayout = viewGroup.findViewById(R.id.additional_content)
    val additionalContentImages: ImagesTableView = viewGroup.findViewById(R.id.additional_content_images)
    val additionalContentPrimaryText: TextView = viewGroup.findViewById(R.id.additional_content_primary_text)
    val additionalContentSecondaryText: TextView = viewGroup.findViewById(R.id.additional_content_secondary_text)
    val additionalContentContext: TextView = viewGroup.findViewById(R.id.additional_content_context)
    val imageTableView: ImagesTableView = viewGroup.findViewById(R.id.tweet_image_container)
    val likeButton: CheckableImageView = viewGroup.findViewById(R.id.tweet_content_like_button)
    val repeatButton: CheckableImageView = viewGroup.findViewById(R.id.tweet_content_retweet_button)
    val replyButton: ImageButton = viewGroup.findViewById(R.id.tweet_content_reply_button)
    val likeCount: TextView = viewGroup.findViewById(R.id.tweet_content_like_count)
    val repeatCount: TextView = viewGroup.findViewById(R.id.tweet_content_retweet_count)
    val repliesCount: TextView = viewGroup.findViewById(R.id.tweet_content_replies_count)

    val pollList: RecyclerView = viewGroup.findViewById(R.id.poll_list)
    val pollAdapter = PollAdapter(viewGroup.context)
    val sendVote: ImageButton = viewGroup.findViewById(R.id.vote_button)
    val pollStatus: TextView = viewGroup.findViewById(R.id.poll_voted_count)

    var onQuotedStatusClicked: View.OnClickListener? = null
    var onCardClicked: View.OnClickListener? = null

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
            additionalContentContext.breakStrategy = Layout.BREAK_STRATEGY_SIMPLE
        }

        additionalContentImages.glideRequests = glideRequests
        imageTableView.glideRequests = glideRequests

        pollList.adapter = pollAdapter
        pollList.layoutManager = LinearLayoutManager(viewGroup.context)
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
            repeatUserName.visibility = View.VISIBLE
            repeatUserName.text = viewGroup.resources.getString(
                    TwitterStringUtils.getRepeatedByStringRes(accessToken.clientType),
                    repeatedUser.name,
                    TwitterStringUtils.plusAtMark(repeatedUser.screenName)
            )
            if (user?.id == accessToken.userId) {
                repeatUserName.typeface = Typeface.DEFAULT_BOLD
            } else {
                repeatUserName.typeface = Typeface.DEFAULT
            }
        } else {
            repeatUserName.visibility = View.GONE
        }

        if (repeated != null) {
            repeatTimeStamp.visibility = View.VISIBLE
            repeatTimeStamp.text = DateUtils.getRelativeTimeSpanString(
                    repeated.createdAt.time,
                    System.currentTimeMillis(),
                    0
            )
        } else {
            repeatTimeStamp.visibility = View.GONE
        }

        val isReply = status.inReplyToScreenName != null
        if (isReply) {
            replyUserName.visibility = View.VISIBLE
            replyUserName.text= if (status.inReplyToScreenName?.isEmpty() == true) {
                viewGroup.resources.getString(
                        if (status.inReplyToStatusId != -1L) {
                            R.string.reply
                        } else {
                            R.string.mention
                        }
                )
            } else {
                viewGroup.resources.getString(
                        if (status.inReplyToStatusId != -1L) {
                            R.string.reply_to
                        } else {
                            R.string.mention_to
                        },
                        TwitterStringUtils.plusAtMark(status.inReplyToScreenName)
                )
            }
        } else {
            replyUserName.visibility = View.GONE
        }

        val timelineImageLoadMode = preferenceRepository.getString(KEY_TIMELINE_IMAGE_LOAD_MODE, "normal")
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
            disposable.add(object: Disposable {
                override fun isDisposed() = false

                override fun dispose() {
                    glideRequests.clear(userName)
                }
            })
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

        val linkedSequence = TwitterStringUtils.getLinkedSequence(viewGroup.context, accessToken, status.text, status.urls)

        tweetContext.text = linkedSequence

        val emojis = status.emojis
        if (!TextUtils.isEmpty(linkedSequence)) {
            tweetContext.visibility = View.VISIBLE

            if (emojis != null) {
                if (contextEmojiSetter == null) {
                    contextEmojiSetter = EmojiToTextViewSetter(glideRequests, tweetContext)
                }
                val set = contextEmojiSetter!!.set(linkedSequence, emojis)
                if (set != null) {
                    disposable.addAll(*set)
                }
            }
        } else {
            tweetContext.visibility = View.GONE
        }

        val text = status.spoilerText
        tweetSpoilerText.text = text
        if (text == null) {
            tweetSpoilerText.visibility = View.GONE
            contentOpenerToggle.visibility = View.GONE
        } else {
            contentOpenerToggle.isChecked = false
            tweetContext.visibility = View.GONE
            tweetSpoilerText.visibility = View.VISIBLE
            contentOpenerToggle.visibility = View.VISIBLE

            if (emojis != null) {
                if (spoilerTextEmojiSetter == null) {
                    spoilerTextEmojiSetter = EmojiToTextViewSetter(glideRequests, tweetSpoilerText)
                }
                val set = spoilerTextEmojiSetter!!.set(text, emojis)
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

        val poll = status.poll

        if (poll != null) {
            pollList.visibility = View.VISIBLE
            pollStatus.visibility = View.VISIBLE
            pollAdapter.setPoll(poll)
            if (poll.expired) {
                pollStatus.text = "${poll.votesCount} votes - closed"
            } else {
                pollStatus.text = "${poll.votesCount} votes - ${poll.expiresAt?.let { DateUtils.getRelativeTimeSpanString(it.time) }}"
            }
            if (poll.expired || poll.voted) {
                sendVote.visibility = View.GONE
            } else {
                sendVote.visibility = View.VISIBLE
            }
        } else {
            pollList.visibility = View.GONE
            pollStatus.visibility = View.GONE
            sendVote.visibility = View.GONE
        }

        val card = status.card

        if (quotedStatus != null && quotedStatusUser != null) {
            additionalContentLayout.visibility = View.VISIBLE
            additionalContentLayout.setOnClickListener(onQuotedStatusClicked)
            additionalContentPrimaryText.text = TwitterStringUtils.plusUserMarks(
                    quotedStatusUser.name,
                    additionalContentPrimaryText,
                    quotedStatusUser.isProtected,
                    quotedStatusUser.isVerified
            )
            val qsMedias = quotedStatus.medias
            if (qsMedias?.isNotEmpty() == true) {
                additionalContentImages.visibility = View.VISIBLE
                additionalContentImages.setMedias(qsMedias, accessToken.clientType, quotedStatus.isSensitive, timelineImageLoadMode)
                disposable.add(object: Disposable {
                    override fun isDisposed() = false

                    override fun dispose() {
                        additionalContentImages.clearImages()
                    }
                })
            } else {
                additionalContentImages.visibility = View.GONE
            }
            additionalContentSecondaryText.text = TwitterStringUtils.plusAtMark(quotedStatusUser.screenName)
            additionalContentContext.text = quotedStatus.text
        } else if (card != null) {
            additionalContentLayout.visibility = View.VISIBLE
            additionalContentLayout.setOnClickListener(onCardClicked)

            additionalContentPrimaryText.text = card.title
            additionalContentSecondaryText.text = card.url.split('/').getOrNull(2)?:""
            additionalContentContext.text = card.description
            val imageUrl = card.imageUrl
            if (imageUrl != null) {
                additionalContentImages.visibility = View.VISIBLE
                additionalContentImages.setMedias(
                        arrayOf(
                                Media(
                                        originalUrl = imageUrl,
                                        mediaType = Media.MediaType.PICTURE.value
                                )
                        ),
                        CLIENT_TYPE_NOTHING,
                        false,
                        timelineImageLoadMode
                )
                disposable.add(object: Disposable {
                    override fun isDisposed() = false

                    override fun dispose() {
                        additionalContentImages.clearImages()
                    }
                })
            } else {
                additionalContentImages.visibility = View.GONE
            }
        } else {
            additionalContentLayout.visibility = View.GONE
            additionalContentLayout.setOnClickListener(null)
        }

        val medias = status.medias

        if (medias?.isNotEmpty() == true) {
            imageTableView.visibility = View.VISIBLE
            imageTableView.setMedias(medias, accessToken.clientType, status.isSensitive, timelineImageLoadMode)

            disposable.add(object: Disposable {
                override fun isDisposed() = false

                override fun dispose() {
                    imageTableView.clearImages()
                }
            })
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
                    repeatIconResourceId = R.drawable.stateful_lock_button
                }
                status.visibility == Visibility.Direct.value -> {
                    isRepeatEnabled = false
                    repeatIconResourceId = R.drawable.stateful_dm_button
                }
                else -> {
                    isRepeatEnabled = true
                    repeatIconResourceId = R.drawable.stateful_repeat_button
                }
            }
        } else {
            if (user != null && (!user.isProtected || user.id == accessToken.userId)) {
                isRepeatEnabled = true
                repeatIconResourceId = R.drawable.stateful_repeat_button
            } else {
                isRepeatEnabled = false
                repeatIconResourceId = R.drawable.stateful_lock_button
            }
        }
        repeatButton.isEnabled = isRepeatEnabled
        repeatButton.setImageDrawable(ContextCompat.getDrawable(viewGroup.context, repeatIconResourceId))

        likeCount.text = if (status.favoriteCount != 0) TwitterStringUtils.convertToSIUnitString(status.favoriteCount, unitsExponent, unitsBack, units) else ""
        repeatCount.text = if (status.repeatCount != 0) TwitterStringUtils.convertToSIUnitString(status.repeatCount, unitsExponent, unitsBack, units) else ""
        repliesCount.text = if (status.repliesCount != 0) TwitterStringUtils.convertToSIUnitString(status.repliesCount, unitsExponent, unitsBack, units) else ""
    }

    fun clear() {
        disposable.clear()
        hasStatus = false
    }

}