package church.authenticcity.android.fragments

import android.animation.Animator
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import church.authenticcity.android.classes.AuthenticElement
import church.authenticcity.android.classes.AuthenticEvent
import church.authenticcity.android.classes.AuthenticEventPlaceholder
import church.authenticcity.android.classes.ButtonAction
import church.authenticcity.android.databinding.FragmentContentBasicBinding
import church.authenticcity.android.helpers.DatabaseHelper
import church.authenticcity.android.helpers.Utils
import church.authenticcity.android.helpers.isNullOrWhiteSpace

class EventFragment : AuthenticFragment<FragmentContentBasicBinding>() {
    companion object {
        fun getInstance(eventId: String, title: String, listener: OnFragmentTitleChangeListener?) = EventFragment().apply {
            arguments = Bundle().apply {
                putString("eventId", eventId)
            }
            setup(title, {i, c, a -> FragmentContentBasicBinding.inflate(i, c, a)}, listener)
        }
    }

    private val eventId: String
        get() = arguments?.getString("eventId", "") ?: ""

    override val root: View
        get() = binding.root

    private fun setContent(content: View) {
        binding.contentList.removeAllViews()
        binding.contentList.addView(content)
        binding.nestedScrollView.animate().alpha(1f).setDuration(125L).setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                binding.swipeRefreshLayout.isRefreshing = false
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }
        }).start()
    }

    private fun setContent(views: Array<View>) {
        binding.contentList.removeAllViews()
        views.forEach { binding.contentList.addView(it) }
        binding.nestedScrollView.animate().alpha(1f).setDuration(125L).setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                binding.swipeRefreshLayout.isRefreshing = false
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }
        }).start()
    }

    private fun setErrorMessage(view: View, message: String) {
        setContent(TextView(view.context).apply {
            text = Utils.makeTypefaceSpan(message, view.context)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.BLACK)
        })
    }

    private fun populate(view: View, event: AuthenticEvent?) {
        if (event == null)
            setErrorMessage(view, "Error 404: The page $eventId could not be found.")
        else {
            title = event.title
            if (event is AuthenticEventPlaceholder)
                setContent(event.convertedElements.map { it.toView(view.context) }.toTypedArray())
            else
                binding.contentList.apply {
                    addView(AuthenticElement.createImage(view.context, event.header, false))
                    addView(AuthenticElement.createTitle(view.context, event.title, "center", size = 32f))
                    addView(AuthenticElement.createText(view.context, event.description, "left", size = 20f))
                    addView(AuthenticElement.createSeparator(view.context, true))
                    addView(AuthenticElement.createTitle(view.context, "Date & Time", "center"))
                    addView(AuthenticElement.createText(view.context, event.getNextOccurrence().format(event.hideEndDate), "left"))
                    if (event.recurs)
                        addView(AuthenticElement.createText(view.context, event.recurrenceRule!!.format(event.startDate, event.endDate), "left"))
                    addView(AuthenticElement.createButton(view.context, ButtonAction(HashMap<String, Any>().apply {
                        put("group", 0)
                        put("type", "AddToCalendarAction")
                        put("eventId", event.id)
                    }), "Add to Calendar"))

                    addView(AuthenticElement.createSeparator(view.context, true))
                    addView(AuthenticElement.createTitle(view.context, "Location", "center"))
                    addView(AuthenticElement.createText(view.context, event.location, "left", selectable = true))
                    if (!String.isNullOrWhiteSpace(event.address)) {
                        addView(AuthenticElement.createText(view.context, event.address, "left", selectable = true))
                        addView(AuthenticElement.createButton(view.context, ButtonAction(HashMap<String, Any>().apply {
                            put("group", -1)
                            put("type", "GetDirectionsAction")
                            put("address", event.address)
                        }), "Get Directions"))
                    } else
                        addView(AuthenticElement.createButton(view.context, ButtonAction(HashMap<String, Any>().apply {
                            put("group", -1)
                            put("type", "ShowMapAction")
                            put("address", event.location)
                        }), "Search"))

                    addView(AuthenticElement.createSeparator(view.context, true))
                    addView(AuthenticElement.createTitle(view.context, "Price & Registration", "center"))
                    if (!String.isNullOrWhiteSpace(event.registrationUrl)) {
                        addView(AuthenticElement.createText(view.context, if (event.price > 0) "$${event.price}" else "Free", "left"))
                        addView(AuthenticElement.createText(view.context, "Registration is required", "left"))
                        addView(AuthenticElement.createButton(view.context, ButtonAction(HashMap<String, Any>().apply {
                            put("group", -1)
                            put("type", "OpenURLAction")
                            put("url", event.registrationUrl)
                        }), "Register Now"))
                    } else {
                        addView(AuthenticElement.createText(view.context, "Free", "left"))
                        addView(AuthenticElement.createText(view.context, "Registration is not required", "left"))
                    }
                    addView(AuthenticElement.createSeparator(view.context, false))
                }
        }
    }

    override fun onRefreshView(view: View) {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.nestedScrollView.animate().alpha(0f).setDuration(125L).setListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(p0: Animator?) {
                DatabaseHelper.loadEvent(eventId, false) {er, ev ->
                    if (er != null)
                        setErrorMessage(view, "Error 400: Bad request.  FB#${er.code}: ${er.message}")
                    else populate(view, ev)

                }
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }
        }).start()
    }

    override fun onCreateView(view: View) {
        binding.swipeRefreshLayout.setOnRefreshListener { onRefresh() }
    }
}