package org.app.corge.data.content

import org.app.corge.data.model.Category

object BundledContent {
    data class SeedMsg(
        val type: String,
        val category: String,
        val text: String,
        val illustrationName: String? = null,
        val ritual: String? = null,
        val whyItMatters: String? = null,
        val recommendedTime: String? = null,
        val durationSeconds: Int? = null,
        val breathingRelated: Boolean = false
    )

    val categories = listOf(
        Category(id = "calm", title = "Calmness", description = null),
        Category(id = "awareness",     title = "Awareness",      description = null),
        Category(id = "purposefulness",title = "Purposefulness", description = null),
        Category(id = "gratitude",     title = "Gratitude",      description = null),
        Category(id = "liberation",    title = "Liberation",     description = null),
        Category(id = "relaxation",    title = "Relaxation",     description = null),
    )

    val messages: List<SeedMsg> = buildList {

        add(SeedMsg("task","Calmness",
            "Sit in a quiet place, take three deep breaths in while mentally saying “I breathe in peace” and exhale while mentally saying “I let go of anxiety”, then stay in silence for one minute and thank yourself for this moment."
        ))
        add(SeedMsg("task","Calmness",
            "Walk slowly, without your phone, observing the sounds, colors and movement around you, as if everything is happening for the first time."
        ))
        add(SeedMsg("task","Calmness",
            "Write three things on a piece of paper that you are grateful for today, and place it next to you as an anchor of peace."
        ))
        add(SeedMsg("task","Calmness",
            "Light a candle, sit next to it and just watch its flame for two minutes, allowing your thoughts to subside."
        ))
        add(SeedMsg("phrase","Calmness",
            "I allow myself to slow down, because clarity is born in silence."
        ))
        add(SeedMsg("phrase","Calmness",
            "Everything I need is already inside me, and I can breathe easy, knowing this."
        ))
        add(SeedMsg("phrase","Calmness",
            "I do not have to respond to the noise of the world – I choose peace and presence."
        ))
        add(SeedMsg("phrase","Calmness",
            "My inner peace is like a lake at dawn: quiet, deep, unshakable."
        ))
        add(SeedMsg("breathing","Calmness",
            "Box Breathing\n\nInhalation — 4 seconds  Delay — 4 seconds  Exhalation — 4 seconds  Delay — 4 seconds. Repeat 4 cycles. Imagine how you mentally draw a square: inhale - up, hold - to the right, exhale - down, hold - to the left.",
            durationSeconds = 60, breathingRelated = true
        ))
        add(SeedMsg("breathing","Calmness",
            "Breathing 4–7–8\n\nInhalation — 4 seconds  Delay — 7 seconds  Exhalation — 8 seconds. Repeat 3–4 times. This breathing slows down the heartbeat and helps to quickly relax, especially before going to sleep or in an anxious moment.",
            durationSeconds = 60, breathingRelated = true
        ))

        add(SeedMsg("task","Awareness",
            "For an hour, observe your thoughts without judging them – just notice how they come and go."
        ))
        add(SeedMsg("task","Awareness",
            "Take a walk without a goal: go where you are drawn and notice the details - colors, sounds, smells."
        ))
        add(SeedMsg("task","Awareness",
            "Write 5 things that you feel right now – physically, emotionally, mentally."
        ))
        add(SeedMsg("task","Awareness",
            "Look in the mirror for 1 minute, without judging yourself – just observe how you are."
        ))
        add(SeedMsg("phrase","Awareness",
            "I am not my thoughts, I am the one who notices them."
        ))
        add(SeedMsg("phrase","Awareness",
            "Everything I feel has a right to be."
        ))
        add(SeedMsg("phrase","Awareness",
            "Awareness begins with a simple “I am here”."
        ))
        add(SeedMsg("phrase","Awareness",
            "I allow myself to be in the moment, unhurried and expected."
        ))
        add(SeedMsg("breathing","Awareness",
            "Breath of the observer\n\nSit quietly and just watch your breath without changing it - inhale, exhale, pause. Do it for 2 minutes.",
            durationSeconds = 120, breathingRelated = true
        ))
        add(SeedMsg("breathing","Awareness",
            "Breathing with body scanning\n\nOn inhalation - attention up the body (from the feet to the head), on exhalation - down. Repeat 5 cycles.",
            durationSeconds = 90, breathingRelated = true
        ))

        add(SeedMsg("task","Purposefulness",
            "Write one goal for today and three small steps that will bring you closer to it."
        ))
        add(SeedMsg("task","Purposefulness",
            "Introduce yourself in an hour – describe what you have achieved, how you look, what you feel."
        ))
        add(SeedMsg("task","Purposefulness",
            "Set aside 15 minutes for a task that you have been putting off for a long time, and just start, without waiting."
        ))
        add(SeedMsg("task","Purposefulness",
            "Make a list of 5 things that inspire you to move forward – people, phrases, images, events."
        ))
        add(SeedMsg("phrase","Purposefulness","I don’t wait for the perfect moment — I create the ego myself."))
        add(SeedMsg("phrase","Purposefulness","My goal is clear, and every step counts."))
        add(SeedMsg("phrase","Purposefulness","I choose movement, even if it is slow."))
        add(SeedMsg("phrase","Purposefulness","Inside me is a force that knows where to go."))
        add(SeedMsg("breathing","Purposefulness",
            "Breathing with intention\n\nOn inhalation - mentally “I am approaching the goal”, on exhalation - “I let go of doubts”. Repeat 5 times.",
            durationSeconds = 90, breathingRelated = true
        ))
        add(SeedMsg("breathing","Purposefulness",
            "Energizing breath\n\n3 quick breaths through the nose, one long exhalation through the mouth. Repeat 4 cycles for awakening and focus.",
            durationSeconds = 60, breathingRelated = true
        ))

        add(SeedMsg("task","Gratitude","Write 3 things for which you are grateful today — even if they are small."))
        add(SeedMsg("task","Gratitude","Remember the person who helped you in life, and mentally thank him."))
        add(SeedMsg("task","Gratitude","Notice something beautiful around you (color, sound, gesture) and give thanks for being able to see it."))
        add(SeedMsg("task","Gratitude","Before going to sleep, say to yourself: “Thank you for this day, for everything that happened, and for what I am.”"))
        add(SeedMsg("phrase","Gratitude","I am grateful for the fact that I can feel, learn and change."))
        add(SeedMsg("phrase","Gratitude","Everything that comes is a gift, even if I don’t understand it right away."))
        add(SeedMsg("phrase","Gratitude","Gratitude is my way of saying yes to life."))
        add(SeedMsg("phrase","Gratitude","I notice the good and let it grow inside me."))
        add(SeedMsg("breathing","Gratitude",
            "Breath of gratitude\n\nOn inhalation - “I accept”, on exhalation - “I thank”. Repeat 5 times, slowly.",
            durationSeconds = 90, breathingRelated = true
        ))
        add(SeedMsg("breathing","Gratitude",
            "Cardiac breathing\n\nPut your hand on your chest, breathe deeply and imagine how with each exhalation you send gratitude to the world.",
            durationSeconds = 90, breathingRelated = true
        ))

        add(SeedMsg("task","Liberation","Write what is bothering you now, and then tear or burn this letter — symbolically releasing it."))
        add(SeedMsg("task","Liberation","Clean one space around you (table, folder, corner) and observe how it affects your inner state."))
        add(SeedMsg("task","Liberation","Take a walk with the intention of letting go – with each exhalation, mentally say: “I am freeing myself.”"))
        add(SeedMsg("task","Liberation","Set aside 10 minutes for silence: no phone, thoughts about the future, problems – just be."))
        add(SeedMsg("phrase","Liberation","I let go of what no longer serves me."))
        add(SeedMsg("phrase","Liberation","Liberation is not a loss, but a return to oneself."))
        add(SeedMsg("phrase","Liberation","I don’t have to carry everything – I choose lightness."))
        add(SeedMsg("phrase","Liberation","Inside me there is a place for the new, because I let go of the old."))
        add(SeedMsg("breathing","Liberation",
            "Breath of liberation\n\nTake a deep breath, and on exhalation imagine how tension, burden, and fatigue go away. Repeat 5 times.",
            durationSeconds = 90, breathingRelated = true
        ))
        add(SeedMsg("breathing","Liberation",
            "Relaxed breathing\n\nOn inhalation - “I accept”, on exhalation - “I release”. Do slowly, consciously, 3–4 cycles.",
            durationSeconds = 90, breathingRelated = true
        ))

        add(SeedMsg("task","Relaxation","Lie down or sit comfortably, turn on calm music and just listen without being distracted by anything."))
        add(SeedMsg("task","Relaxation","Take a warm shower or bath, imagining how the water washes away fatigue and tension."))
        add(SeedMsg("task","Relaxation","Make 10 slow circular motions with your shoulders back, then forward — with full attention to sensations."))
        add(SeedMsg("task","Relaxation","Spend 5 minutes looking out the window, watching the movement of clouds, leaves or light."))
        add(SeedMsg("phrase","Relaxation","I allow myself to slow down and do nothing."))
        add(SeedMsg("phrase","Relaxation","My body knows how to relax – I just don’t bother it."))
        add(SeedMsg("phrase","Relaxation","In peace, I recover and replenish myself."))
        add(SeedMsg("phrase","Relaxation","I let go of the rush and return to myself."))
        add(SeedMsg("breathing","Relaxation",
            "Soft breathing\n\nInhale through the nose for 4 seconds, exhale through the mouth for 6 seconds. Repeat 5 times, with a feeling of softness.",
            durationSeconds = 90, breathingRelated = true
        ))
        add(SeedMsg("breathing","Relaxation",
            "Breathing with the body\n\nOn inhalation - attention to the stomach, on exhalation - to the shoulders and neck. Feel how the body becomes lighter.",
            durationSeconds = 90, breathingRelated = true
        ))
    }
}
