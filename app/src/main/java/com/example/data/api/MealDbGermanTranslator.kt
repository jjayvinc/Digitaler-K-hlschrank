package com.example.data.api

import java.util.Locale

object MealDbGermanTranslator {

    val areaTranslations = mapOf(
        "American" to "Amerikanisch",
        "British" to "Britisch",
        "Canadian" to "Kanadisch",
        "Chinese" to "Chinesisch",
        "Croatian" to "Kroatisch",
        "Dutch" to "Niederländisch",
        "Egyptian" to "Ägyptisch",
        "Filipino" to "Philippinisch",
        "French" to "Französisch",
        "Greek" to "Griechisch",
        "Indian" to "Indisch",
        "Irish" to "Irisch",
        "Italian" to "Italienisch",
        "Jamaican" to "Jamaikanisch",
        "Japanese" to "Japanisch",
        "Kenyan" to "Kenianisch",
        "Malaysian" to "Malaysisch",
        "Mexican" to "Mexikanisch",
        "Moroccan" to "Marokkanisch",
        "Polish" to "Polnisch",
        "Portuguese" to "Portugiesisch",
        "Russian" to "Russisch",
        "Spanish" to "Spanisch",
        "Thai" to "Thailändisch",
        "Tunisian" to "Tunesisch",
        "Turkish" to "Türkisch",
        "Ukrainian" to "Ukrainisch",
        "Vietnamese" to "Vietnamesisch"
    )

    val categoryTranslations = mapOf(
        "Beef" to "Rindfleisch & Steaks",
        "Chicken" to "Geflügel & Hähnchen",
        "Dessert" to "Desserts & Süßspeisen",
        "Lamb" to "Lammfleisch",
        "Miscellaneous" to "Vielfältige Gerichte",
        "Pasta" to "Pasta & Nudeln",
        "Pork" to "Schweinefleisch",
        "Seafood" to "Fisch & Meeresfrüchte",
        "Side" to "Beilagen",
        "Starter" to "Vorspeisen & Snacks",
        "Vegan" to "Vegan",
        "Vegetarian" to "Vegetarisch",
        "Breakfast" to "Frühstück",
        "Goat" to "Traditionelles Fleisch"
    )

    private val directTitleTranslations = mapOf(
        "teriyaki chicken casserole" to "Teriyaki-Hähnchen-Auflauf",
        "mediterranean pasta salad" to "Mediterraner Nudelsalat",
        "spaghetti bolognese" to "Spaghetti Bolognese",
        "lasagne" to "Klassische Lasagne",
        "beef and mustard pie" to "Rindfleisch-Senf-Pastete",
        "beef and broccoli" to "Rindfleisch mit Brokkoli",
        "chicken alfredo" to "Cremiges Hähnchen Alfredo",
        "chicken fajitas" to "Würzige Hähnchen-Fajitas",
        "chicken marengo" to "Hähnchen Marengo",
        "chicken handi" to "Hähnchen Handi Curry",
        "pork chops with apples" to "Schweinekoteletts mit Äpfeln",
        "chili con carne" to "Feuriges Chili con Carne",
        "apple & blackberry crumble" to "Apfel-Brombeer-Crumble",
        "apple frangipan tart" to "Apfel-Mandel-Tarte",
        "pancakes" to "Fluffige Pfannkuchen",
        "banana pancakes" to "Bananen-Pfannkuchen",
        "french onion soup" to "Französische Zwiebelsuppe",
        "greek salad" to "Griechischer Bauernsalat",
        "thai green curry" to "Grünes Thai-Curry",
        "pad thai" to "Klassisches Pad Thai",
        "beef stroganoff" to "Bœuf Stroganoff",
        "beef bourguignon" to "Boeuf Bourguignon",
        "beef sunday roast" to "Rinder-Sonntagsbraten",
        "salmon en croute" to "Lachsfilet im Blätterteig",
        "fish and chips" to "Knuspriger Fisch & Pommes",
        "fish pie" to "Englischer Fischauflauf",
        "mushroom risotto" to "Cremiges Waldpilz-Risotto",
        "chocolate gateau" to "Schokoladentorte",
        "caesar salad" to "Caesar Salat mit Croûtons",
        "chicken tikka masala" to "Hähnchen Tikka Masala",
        "sweet and sour chicken" to "Hähnchen süß-sauer",
        "crispy pork belly" to "Knuspriger Schweinebauch",
        "vegetable curry" to "Aromatisches Gemüse-Curry",
        "tomato soup" to "Klassische Tomatensuppe",
        "potato soup" to "Herzhafte Kartoffelsuppe",
        "stew" to "Herzhafter Eintopf"
    )

    private val titleWordReplacements = listOf(
        Regex("""(?i)\bchicken breasts?\b""") to "Hähnchenbrust",
        Regex("""(?i)\bchicken thighs?\b""") to "Hähnchenkeulen",
        Regex("""(?i)\bchicken\b""") to "Hähnchen",
        Regex("""(?i)\bbeef\b""") to "Rindfleisch",
        Regex("""(?i)\bpork\b""") to "Schweinefleisch",
        Regex("""(?i)\blamb\b""") to "Lamm",
        Regex("""(?i)\bsalmon\b""") to "Lachs",
        Regex("""(?i)\btuna\b""") to "Thunfisch",
        Regex("""(?i)\bprawns?\b""") to "Garnelen",
        Regex("""(?i)\bshrimps?\b""") to "Garnelen",
        Regex("""(?i)\bfish\b""") to "Fisch",
        Regex("""(?i)\bcasserole\b""") to "Auflauf",
        Regex("""(?i)\bpie\b""") to "Pastete",
        Regex("""(?i)\btart\b""") to "Tarte",
        Regex("""(?i)\bcake\b""") to "Kuchen",
        Regex("""(?i)\bsoup\b""") to "Suppe",
        Regex("""(?i)\bsalad\b""") to "Salat",
        Regex("""(?i)\bstew\b""") to "Eintopf",
        Regex("""(?i)\bcurry\b""") to "Curry",
        Regex("""(?i)\broast\b""") to "Braten",
        Regex("""(?i)\bburgers?\b""") to "Burger",
        Regex("""(?i)\bnoodles?\b""") to "Nudeln",
        Regex("""(?i)\brice\b""") to "Reis",
        Regex("""(?i)\bpasta\b""") to "Pasta",
        Regex("""(?i)\bmushrooms?\b""") to "Champignons",
        Regex("""(?i)\bpotatoes?\b""") to "Kartoffeln",
        Regex("""(?i)\btomatoes?\b""") to "Tomaten",
        Regex("""(?i)\bgarlic\b""") to "Knoblauch",
        Regex("""(?i)\bcheese\b""") to "Käse",
        Regex("""(?i)\beggs?\b""") to "Eier",
        Regex("""(?i)\bbread\b""") to "Brot",
        Regex("""(?i)\bapples?\b""") to "Apfel",
        Regex("""(?i)\bchocolate\b""") to "Schokolade",
        Regex("""(?i)\bcreamy\b""") to "Cremig",
        Regex("""(?i)\bcrispy\b""") to "Knusprig",
        Regex("""(?i)\bspicy\b""") to "Pikant",
        Regex("""(?i)\bbaked\b""") to "Gebacken",
        Regex("""(?i)\bfried\b""") to "Gebraten",
        Regex("""(?i)\bgrilled\b""") to "Gegrillt",
        Regex("""(?i)\broasted\b""") to "Geröstet",
        Regex("""(?i)\bslow cooker\b""") to "Schongarer",
        Regex("""(?i)\bwith\b""") to "mit",
        Regex("""(?i)\band\b""") to "und",
        Regex("""(?i)\bin\b""") to "in",
        Regex("""(?i)\bsauce\b""") to "Sauce"
    )

    fun translateTitle(rawTitle: String): String {
        val trimmed = rawTitle.trim()
        if (trimmed.isEmpty()) return "Rezept"
        val direct = directTitleTranslations[trimmed.lowercase()]
        if (direct != null) return direct

        var result = trimmed
        for ((pattern, de) in titleWordReplacements) {
            result = result.replace(pattern, de)
        }
        return result.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.GERMAN) else it.toString() }
    }

    fun translateArea(rawArea: String): String {
        val trimmed = rawArea.trim()
        return areaTranslations[trimmed] ?: trimmed.ifEmpty { "International" }
    }

    fun translateCategory(rawCategory: String): String {
        val trimmed = rawCategory.trim()
        return categoryTranslations[trimmed] ?: trimmed.ifEmpty { "Sonstiges" }
    }

    // Extensive ingredient dictionary
    val ingredientTranslations = mapOf(
        "chicken" to "Hähnchen",
        "chicken breast" to "Hähnchenbrust",
        "chicken breasts" to "Hähnchenbrust",
        "chicken thigh" to "Hähnchenschenkel",
        "chicken thighs" to "Hähnchenschenkel",
        "chicken wings" to "Hähnchenflügel",
        "chicken stock" to "Hühnerbrühe",
        "chicken broth" to "Hühnerbrühe",
        "beef" to "Rindfleisch",
        "minced beef" to "Hackfleisch",
        "ground beef" to "Hackfleisch",
        "beef steak" to "Rindersteak",
        "beef stock" to "Rinderbrühe",
        "beef broth" to "Rinderbrühe",
        "pork" to "Schweinefleisch",
        "pork chops" to "Schweinekoteletts",
        "pork mince" to "Schweinehack",
        "ground pork" to "Schweinehack",
        "bacon" to "Speck",
        "pancetta" to "Pancetta",
        "ham" to "Schinken",
        "sausage" to "Würstchen",
        "sausages" to "Würstchen",
        "egg" to "Ei",
        "eggs" to "Eier",
        "egg yolk" to "Eigelb",
        "egg yolks" to "Eigelb",
        "egg white" to "Eiweiß",
        "egg whites" to "Eiweiß",
        "milk" to "Milch",
        "whole milk" to "Vollmilch",
        "butter" to "Butter",
        "unsalted butter" to "Ungesalzene Butter",
        "cheese" to "Käse",
        "cheddar cheese" to "Cheddar",
        "cheddar" to "Cheddar",
        "parmesan" to "Parmesan",
        "parmesan cheese" to "Parmesan",
        "mozzarella" to "Mozzarella",
        "feta" to "Feta",
        "ricotta" to "Ricotta",
        "cream cheese" to "Frischkäse",
        "sour cream" to "Sauerrahm",
        "heavy cream" to "Schlagsahne",
        "double cream" to "Schlagsahne",
        "cream" to "Sahne",
        "creme fraiche" to "Crème fraîche",
        "greek yogurt" to "Griechischer Joghurt",
        "yogurt" to "Joghurt",
        "onion" to "Zwiebel",
        "onions" to "Zwiebeln",
        "red onion" to "Rote Zwiebel",
        "red onions" to "Rote Zwiebeln",
        "spring onion" to "Frühlingszwiebel",
        "spring onions" to "Frühlingszwiebeln",
        "green onions" to "Frühlingszwiebeln",
        "scallions" to "Frühlingszwiebeln",
        "shallots" to "Schalotten",
        "shallot" to "Schalotte",
        "garlic" to "Knoblauch",
        "garlic clove" to "Knoblauchzehe",
        "garlic cloves" to "Knoblauchzehen",
        "ginger" to "Ingwer",
        "fresh ginger" to "Frischer Ingwer",
        "tomato" to "Tomate",
        "tomatoes" to "Tomaten",
        "cherry tomatoes" to "Cherrytomaten",
        "plum tomatoes" to "Flaschentomaten",
        "tomato puree" to "Tomatenmark",
        "tomato paste" to "Tomatenmark",
        "chopped tomatoes" to "Gehackte Tomaten",
        "canned tomatoes" to "Tomatendose",
        "passata" to "Passierte Tomaten",
        "potato" to "Kartoffel",
        "potatoes" to "Kartoffeln",
        "sweet potato" to "Süßkartoffel",
        "sweet potatoes" to "Süßkartoffeln",
        "carrot" to "Möhre",
        "carrots" to "Möhren",
        "celery" to "Staudensellerie",
        "celeriac" to "Knollensellerie",
        "broccoli" to "Brokkoli",
        "cauliflower" to "Blumenkohl",
        "cabbage" to "Kohl",
        "spinach" to "Spinat",
        "baby spinach" to "Babyspinat",
        "lettuce" to "Kopfsalat",
        "cucumber" to "Gurke",
        "zucchini" to "Zucchini",
        "courgettes" to "Zucchini",
        "eggplant" to "Aubergine",
        "aubergine" to "Aubergine",
        "bell pepper" to "Paprika",
        "bell peppers" to "Paprika",
        "red pepper" to "Rote Paprika",
        "green pepper" to "Grüne Paprika",
        "yellow pepper" to "Gelbe Paprika",
        "mushrooms" to "Champignons",
        "button mushrooms" to "Weiße Champignons",
        "corn" to "Mais",
        "sweetcorn" to "Zuckermais",
        "peas" to "Erbsen",
        "green beans" to "Grüne Bohnen",
        "kidney beans" to "Kidneybohnen",
        "black beans" to "Schwarze Bohnen",
        "chickpeas" to "Kichererbsen",
        "lentils" to "Linsen",
        "red lentils" to "Rote Linsen",
        "avocado" to "Avocado",
        "lemon" to "Zitrone",
        "lemons" to "Zitronen",
        "lemon juice" to "Zitronensaft",
        "lemon zest" to "Zitronenabrieb",
        "lime" to "Limette",
        "limes" to "Limetten",
        "lime juice" to "Limettensaft",
        "apple" to "Apfel",
        "apples" to "Äpfel",
        "banana" to "Banane",
        "bananas" to "Bananen",
        "strawberries" to "Erdbeeren",
        "raspberries" to "Himbeeren",
        "blueberries" to "Blaubeeren",
        "pasta" to "Pasta",
        "spaghetti" to "Spaghetti",
        "penne" to "Penne",
        "fusilli" to "Fusilli",
        "macaroni" to "Makkaroni",
        "lasagne sheets" to "Lasagneplatten",
        "noodles" to "Nudeln",
        "egg noodles" to "Eiernudeln",
        "rice noodles" to "Reisnudeln",
        "rice" to "Reis",
        "basmati rice" to "Basmatireis",
        "jasmine rice" to "Jasminreis",
        "brown rice" to "Vollkornreis",
        "bread" to "Brot",
        "sliced bread" to "Toastbrot",
        "breadcrumbs" to "Paniermehl",
        "flour" to "Mehl",
        "plain flour" to "Weizenmehl",
        "all-purpose flour" to "Weizenmehl",
        "self-raising flour" to "Backmehl",
        "cornstarch" to "Speisestärke",
        "cornflour" to "Speisestärke",
        "sugar" to "Zucker",
        "white sugar" to "Weißer Zucker",
        "caster sugar" to "Feiner Zucker",
        "brown sugar" to "Brauner Zucker",
        "powdered sugar" to "Puderzucker",
        "icing sugar" to "Puderzucker",
        "baking powder" to "Backpulver",
        "baking soda" to "Natron",
        "yeast" to "Hefe",
        "dry yeast" to "Trockenhefe",
        "cocoa powder" to "Kakaopulver",
        "chocolate" to "Schokolade",
        "dark chocolate" to "Zartbitterschokolade",
        "milk chocolate" to "Vollmilchschokolade",
        "vanilla extract" to "Vanilleextrakt",
        "vanilla" to "Vanille",
        "olive oil" to "Olivenöl",
        "extra virgin olive oil" to "Natives Olivenöl",
        "vegetable oil" to "Pflanzenöl",
        "sunflower oil" to "Sonnenblumenöl",
        "sesame oil" to "Sesamöl",
        "oil" to "Öl",
        "salt" to "Salz",
        "sea salt" to "Meersalz",
        "pepper" to "Pfeffer",
        "black pepper" to "Schwarzer Pfeffer",
        "white pepper" to "Weißer Pfeffer",
        "paprika" to "Paprikapulver",
        "smoked paprika" to "Geräuchertes Paprikapulver",
        "chili powder" to "Chilipulver",
        "chilli flakes" to "Chiliflocken",
        "red pepper flakes" to "Chiliflocken",
        "cayenne pepper" to "Cayennepfeffer",
        "cumin" to "Kreuzkümmel",
        "ground cumin" to "Kreuzkümmel gemahlen",
        "coriander" to "Koriander",
        "ground coriander" to "Koriander gemahlen",
        "cinnamon" to "Zimt",
        "ground cinnamon" to "Gemahlener Zimt",
        "nutmeg" to "Muskatnuss",
        "turmeric" to "Kurkuma",
        "curry powder" to "Currypulver",
        "garam masala" to "Garam Masala",
        "oregano" to "Oregano",
        "dried oregano" to "Getrockneter Oregano",
        "basil" to "Basilikum",
        "fresh basil" to "Frisches Basilikum",
        "parsley" to "Petersilie",
        "fresh parsley" to "Frische Petersilie",
        "cilantro" to "Koriandergrün",
        "fresh coriander" to "Frischer Koriander",
        "thyme" to "Thymian",
        "dried thyme" to "Getrockneter Thymian",
        "rosemary" to "Rosmarin",
        "fresh rosemary" to "Frischer Rosmarin",
        "bay leaf" to "Lorbeerblatt",
        "bay leaves" to "Lorbeerblätter",
        "soy sauce" to "Sojasauce",
        "dark soy sauce" to "Dunkle Sojasauce",
        "light soy sauce" to "Helle Sojasauce",
        "fish sauce" to "Fischsauce",
        "oyster sauce" to "Austernsauce",
        "worcestershire sauce" to "Worcestersauce",
        "vinegar" to "Essig",
        "white wine vinegar" to "Weißweinessig",
        "red wine vinegar" to "Rotweinessig",
        "apple cider vinegar" to "Apfelessig",
        "balsamic vinegar" to "Balsamico-Essig",
        "mustard" to "Senf",
        "dijon mustard" to "Dijon-Senf",
        "wholegrain mustard" to "Körniger Senf",
        "mayonnaise" to "Mayonnaise",
        "ketchup" to "Ketchup",
        "honey" to "Honig",
        "maple syrup" to "Ahornsirup",
        "vegetable stock" to "Gemüsebrühe",
        "vegetable broth" to "Gemüsebrühe",
        "water" to "Wasser",
        "coconut milk" to "Kokosmilch",
        "salmon" to "Lachs",
        "tuna" to "Thunfisch",
        "prawns" to "Garnelen",
        "shrimp" to "Garnelen",
        "almonds" to "Mandeln",
        "walnuts" to "Walnüsse",
        "peanuts" to "Erdnüsse",
        "peanut butter" to "Erdnussbutter",
        "olives" to "Oliven",
        "black olives" to "Schwarze Oliven",
        "green olives" to "Grüne Oliven",
        "capers" to "Kapern"
    )

    fun translateIngredient(raw: String): String {
        val clean = raw.trim().lowercase()
        val direct = ingredientTranslations[clean]
        if (direct != null) return direct

        for ((en, de) in ingredientTranslations) {
            if (clean == en || clean.startsWith("$en ") || clean.endsWith(" $en") || clean.contains(" $en ")) {
                return de
            }
        }
        return raw.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.GERMAN) else it.toString() }
    }

    /**
     * Translates English cooking instructions into fluent, helpful German.
     */
    fun translateInstructions(instructions: String): String {
        if (instructions.isBlank()) return "Keine Zubereitungsschritte angegeben."

        var text = instructions
            // Clean up YouTube or source links
            .replace(Regex("""https?://\S+"""), "")
            .replace("\r\n", "\n")

        // Temperature conversions
        text = text.replace(Regex("""(?i)\b(\d{3})\s*°?\s*F\b""")) { match ->
            val f = match.groupValues[1].toIntOrNull() ?: 350
            val c = ((f - 32) * 5 / 9)
            "${c}°C (${f}°F)"
        }

        // Cooking phrases mapping
        val phraseReplacements = listOf(
            Regex("""(?i)\bpreheat (?:the )?oven to\b""") to "Den Backofen vorheizen auf",
            Regex("""(?i)\bbring a (?:large )?pot of (?:salted )?water to (?:the )?boil\b""") to "Einen großen Topf mit Salzwasser zum Kochen bringen",
            Regex("""(?i)\bheat (?:the )?oil in a (?:large )?(?:skillet|pan)\b""") to "Das Öl in einer großen Pfanne erhitzen",
            Regex("""(?i)\bheat (?:the )?butter in a (?:large )?(?:skillet|pan)\b""") to "Die Butter in einer Pfanne zerlassen",
            Regex("""(?i)\bover medium heat\b""") to "bei mittlerer Hitze",
            Regex("""(?i)\bover high heat\b""") to "bei starker Hitze",
            Regex("""(?i)\bover low heat\b""") to "bei schwacher Hitze",
            Regex("""(?i)\bseason with salt and pepper\b""") to "Mit Salz und Pfeffer abschmecken",
            Regex("""(?i)\bto taste\b""") to "nach Belieben",
            Regex("""(?i)\bcook until golden brown\b""") to "Goldbraun anbraten",
            Regex("""(?i)\bcook until browned\b""") to "Rundum scharf anbraten",
            Regex("""(?i)\bstir occasionally\b""") to "Gelegentlich umrühren",
            Regex("""(?i)\bstir well\b""") to "Gründlich umrühren",
            Regex("""(?i)\bstir in the\b""") to "Einrühren:",
            Regex("""(?i)\badd the\b""") to "Hinzugeben:",
            Regex("""(?i)\bremove from heat\b""") to "Vom Herd nehmen",
            Regex("""(?i)\bdrain and set aside\b""") to "Abgießen und beiseitestellen",
            Regex("""(?i)\bset aside\b""") to "beiseitestellen",
            Regex("""(?i)\bserve immediately\b""") to "Sofort heiß servieren",
            Regex("""(?i)\bserve hot\b""") to "Heiß servieren",
            Regex("""(?i)\bserve warm\b""") to "Warm servieren",
            Regex("""(?i)\bgarnish with\b""") to "Garnieren mit",
            Regex("""(?i)\bsimmer for (\d+) minutes\b""") to "$1 Minuten köcheln lassen",
            Regex("""(?i)\bbake for (\d+) minutes\b""") to "$1 Minuten backen",
            Regex("""(?i)\bcook for (\d+) minutes\b""") to "$1 Minuten garen",
            Regex("""(?i)\bminutes\b""") to "Minuten",
            Regex("""(?i)\bminute\b""") to "Minute",
            Regex("""(?i)\bhours\b""") to "Stunden",
            Regex("""(?i)\bhour\b""") to "Stunde",
            Regex("""(?i)\bstep (\d+)\b""") to "Schritt $1:",
            Regex("""(?i)\benjoy\b!?""") to "Guten Appetit!"
        )

        for ((regex, replacement) in phraseReplacements) {
            text = text.replace(regex, replacement)
        }

        // Common culinary verbs & words
        val wordReplacements = listOf(
            Regex("""(?i)\bchop\b""") to "fein schneiden",
            Regex("""(?i)\bdice\b""") to "in Würfel schneiden",
            Regex("""(?i)\bmince\b""") to "fein hacken",
            Regex("""(?i)\bwhisk\b""") to "mit dem Schneebesen verquirlen",
            Regex("""(?i)\bmelt\b""") to "schmelzen",
            Regex("""(?i)\bboil\b""") to "kochen",
            Regex("""(?i)\bsimmer\b""") to "köcheln lassen",
            Regex("""(?i)\bfry\b""") to "anbraten",
            Regex("""(?i)\bbake\b""") to "backen",
            Regex("""(?i)\broast\b""") to "rösten",
            Regex("""(?i)\bgrill\b""") to "grillen",
            Regex("""(?i)\bcover\b""") to "abdecken",
            Regex("""(?i)\buncover\b""") to "ohne Deckel",
            Regex("""(?i)\bbowl\b""") to "Schüssel",
            Regex("""(?i)\bpan\b""") to "Pfanne",
            Regex("""(?i)\bskillet\b""") to "Bratpfanne",
            Regex("""(?i)\bpot\b""") to "Topf",
            Regex("""(?i)\boven\b""") to "Ofen",
            Regex("""(?i)\bwater\b""") to "Wasser"
        )

        for ((regex, replacement) in wordReplacements) {
            text = text.replace(regex, replacement)
        }

        return text.trim()
    }
}
