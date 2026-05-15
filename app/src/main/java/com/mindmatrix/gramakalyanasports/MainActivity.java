package com.mindmatrix.gramakalyanasports;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "grama_kalyana_sports";
    private final String[] sports = {"Kabaddi", "Volleyball", "Cricket"};

    private SharedPreferences prefs;
    private LinearLayout content;
    private LinearLayout nav;

    private String currentTab = "setup";
    private String sport = "Kabaddi";
    private String teamA = "Kalyana Warriors";
    private String teamB = "Grama Strikers";
    private String schedule = "Today, 5:00 PM - Village Ground";
    private int scoreA = 0;
    private int scoreB = 0;
    private int wicketsA = 0;
    private int wicketsB = 0;
    private int oversA = 0;
    private int oversB = 0;
    private int activeTeam = 0;
    private String manOfMatch = "Not selected";
    private final List<String> players = new ArrayList<>();
    private final Map<String, Integer> playerPoints = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        loadState();
        buildShell();
        showTab(currentTab);
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(this);
        title.setText("Grama-Kalyana Sports");
        title.setTextColor(Color.WHITE);
        title.setTextSize(26);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(dp(16), dp(18), dp(16), dp(6));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("Digital scoreboard for local tournaments");
        subtitle.setTextColor(Color.rgb(255, 212, 0));
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(dp(12), 0, dp(12), dp(12));
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, -2));

        nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(dp(8), dp(4), dp(8), dp(8));
        root.addView(nav, new LinearLayout.LayoutParams(-1, -2));
        addNavButton("Setup", "setup");
        addNavButton("Scorer", "scorer");
        addNavButton("Fan View", "fan");
        addNavButton("Stats", "stats");

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(8), dp(14), dp(18));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void addNavButton(String label, final String tab) {
        Button button = button(label, 13);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTab(tab);
            }
        });
        nav.addView(button, new LinearLayout.LayoutParams(0, dp(46), 1));
    }

    private void showTab(String tab) {
        currentTab = tab;
        content.removeAllViews();
        refreshNav();
        if ("setup".equals(tab)) {
            renderSetup();
        } else if ("scorer".equals(tab)) {
            renderScorer();
        } else if ("fan".equals(tab)) {
            renderFan();
        } else {
            renderStats();
        }
        saveState();
    }

    private void refreshNav() {
        for (int i = 0; i < nav.getChildCount(); i++) {
            Button child = (Button) nav.getChildAt(i);
            String text = child.getText().toString().toLowerCase().replace(" ", "");
            boolean selected = currentTab.equals(text) || ("fan".equals(currentTab) && "fanview".equals(text));
            child.setBackgroundColor(selected ? Color.rgb(255, 212, 0) : Color.rgb(35, 35, 35));
            child.setTextColor(selected ? Color.BLACK : Color.WHITE);
        }
    }

    private void renderSetup() {
        heading("Tournament Setup");
        small("Create the match once, then use Scorer and Fan View during the game.");

        label("Sport");
        final Spinner sportSpinner = new Spinner(this);
        ArrayAdapter<String> sportAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sports);
        sportSpinner.setAdapter(sportAdapter);
        sportSpinner.setSelection(indexOf(sports, sport));
        content.addView(sportSpinner, blockParams());

        final EditText teamAInput = input(teamA, "Team A name");
        final EditText teamBInput = input(teamB, "Team B name");
        final EditText scheduleInput = input(schedule, "Schedule and venue");
        final EditText playersInput = input(playersAsText(), "Players, one per line");
        playersInput.setMinLines(5);

        label("Team A");
        content.addView(teamAInput, blockParams());
        label("Team B");
        content.addView(teamBInput, blockParams());
        label("Schedule");
        content.addView(scheduleInput, blockParams());
        label("Players");
        content.addView(playersInput, blockParams());

        Button save = button("Save Tournament", 16);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sport = sportSpinner.getSelectedItem().toString();
                teamA = cleanOrDefault(teamAInput.getText().toString(), "Team A");
                teamB = cleanOrDefault(teamBInput.getText().toString(), "Team B");
                schedule = cleanOrDefault(scheduleInput.getText().toString(), "Village Ground");
                applyPlayers(playersInput.getText().toString());
                saveState();
                Toast.makeText(MainActivity.this, "Tournament saved", Toast.LENGTH_SHORT).show();
                showTab("scorer");
            }
        });
        content.addView(save, blockParams());

        Button reset = button("Reset Scores Only", 16);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scoreA = 0; scoreB = 0; wicketsA = 0; wicketsB = 0; oversA = 0; oversB = 0;
                saveState();
                Toast.makeText(MainActivity.this, "Scores reset", Toast.LENGTH_SHORT).show();
                showTab("setup");
            }
        });
        content.addView(reset, blockParams());
    }

    private void renderScorer() {
        heading("Live Scorer");
        scoreboard(false);

        LinearLayout teamRow = new LinearLayout(this);
        teamRow.setOrientation(LinearLayout.HORIZONTAL);
        Button a = button(teamA, 15);
        Button b = button(teamB, 15);
        a.setBackgroundColor(activeTeam == 0 ? Color.rgb(255, 212, 0) : Color.DKGRAY);
        b.setBackgroundColor(activeTeam == 1 ? Color.rgb(255, 212, 0) : Color.DKGRAY);
        a.setTextColor(activeTeam == 0 ? Color.BLACK : Color.WHITE);
        b.setTextColor(activeTeam == 1 ? Color.BLACK : Color.WHITE);
        a.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { activeTeam = 0; showTab("scorer"); }
        });
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { activeTeam = 1; showTab("scorer"); }
        });
        teamRow.addView(a, new LinearLayout.LayoutParams(0, dp(52), 1));
        teamRow.addView(b, new LinearLayout.LayoutParams(0, dp(52), 1));
        content.addView(teamRow, blockParams());

        LinearLayout points = new LinearLayout(this);
        points.setOrientation(LinearLayout.HORIZONTAL);
        points.addView(scoreButton("+1", 1), new LinearLayout.LayoutParams(0, dp(58), 1));
        points.addView(scoreButton("+2", 2), new LinearLayout.LayoutParams(0, dp(58), 1));
        points.addView(scoreButton("+3", 3), new LinearLayout.LayoutParams(0, dp(58), 1));
        points.addView(scoreButton("-1", -1), new LinearLayout.LayoutParams(0, dp(58), 1));
        content.addView(points, blockParams());

        if ("Cricket".equals(sport)) {
            LinearLayout cricket = new LinearLayout(this);
            cricket.setOrientation(LinearLayout.HORIZONTAL);
            Button wicket = button("Wicket", 15);
            wicket.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (activeTeam == 0) wicketsA = Math.min(10, wicketsA + 1);
                    else wicketsB = Math.min(10, wicketsB + 1);
                    showTab("scorer");
                }
            });
            Button over = button("Over +1", 15);
            over.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (activeTeam == 0) oversA++; else oversB++;
                    showTab("scorer");
                }
            });
            cricket.addView(wicket, new LinearLayout.LayoutParams(0, dp(54), 1));
            cricket.addView(over, new LinearLayout.LayoutParams(0, dp(54), 1));
            content.addView(cricket, blockParams());
        }

        label("Player contribution");
        for (final String player : players) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundColor(Color.WHITE);
            row.setPadding(dp(12), dp(8), dp(8), dp(8));

            TextView nameTxt = new TextView(this);
            int p = playerPoints.containsKey(player) ? playerPoints.get(player) : 0;
            nameTxt.setText(player + "\nPoints: " + p);
            nameTxt.setTextColor(Color.BLACK);
            nameTxt.setTextSize(16);
            nameTxt.setTypeface(Typeface.DEFAULT_BOLD);

            Button add = button("+1", 18);
            add.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    int val = playerPoints.containsKey(player) ? playerPoints.get(player) : 0;
                    playerPoints.put(player, val + 1);
                    showTab("scorer");
                }
            });

            row.addView(nameTxt, new LinearLayout.LayoutParams(0, -2, 1));
            row.addView(add, new LinearLayout.LayoutParams(dp(84), dp(52)));
            content.addView(row, blockParams());
        }

        Button share = button("Export Scorecard", 16);
        share.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { shareScorecard(); }
        });
        content.addView(share, blockParams());
    }

    private void renderFan() {
        heading("Fan Live View");
        scoreboard(true);
        TextView summary = card();
        summary.setText(makeMatchSummary());
        content.addView(summary, blockParams());
        Button refresh = button("Refresh Live Score", 16);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { loadState(); showTab("fan"); }
        });
        content.addView(refresh, blockParams());
    }

    private void renderStats() {
        heading("Player Stats");
        if (!players.isEmpty()) {
            final Spinner motmSpinner = new Spinner(this);
            motmSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, players));
            int selected = players.indexOf(manOfMatch);
            if (selected >= 0) motmSpinner.setSelection(selected);
            label("Man of the Match");
            content.addView(motmSpinner, blockParams());

            Button setMotm = button("Set Man of the Match", 16);
            setMotm.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    manOfMatch = motmSpinner.getSelectedItem().toString();
                    showTab("stats");
                }
            });
            content.addView(setMotm, blockParams());
        }
        TextView motm = card();
        motm.setText("Man of the Match\n" + manOfMatch);
        content.addView(motm, blockParams());
        for (String player : players) {
            TextView stat = card();
            int p = playerPoints.containsKey(player) ? playerPoints.get(player) : 0;
            stat.setText(player + "\nCareer points: " + p);
            content.addView(stat, blockParams());
        }
    }

    private Button scoreButton(String label, final int delta) {
        Button b = button(label, 22);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (activeTeam == 0) scoreA = Math.max(0, scoreA + delta);
                else scoreB = Math.max(0, scoreB + delta);
                showTab("scorer");
            }
        });
        return b;
    }

    private void scoreboard(boolean large) {
        TextView meta = card();
        meta.setText(sport + "\n" + schedule);
        meta.setTextSize(large ? 18 : 16);
        content.addView(meta, blockParams());
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(scoreCard(teamA, scoreA, wicketsA, oversA, large), new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(scoreCard(teamB, scoreB, wicketsB, oversB, large), new LinearLayout.LayoutParams(0, -2, 1));
        content.addView(row, blockParams());
    }

    private TextView scoreCard(String team, int score, int wickets, int overs, boolean large) {
        TextView v = card();
        String extra = "Cricket".equals(sport) ? "\n" + wickets + "W, " + overs + " Ov" : "";
        v.setText(team + "\n" + score + extra);
        v.setGravity(Gravity.CENTER);
        v.setTextSize(large ? 34 : 24);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setMinHeight(dp(large ? 180 : 130));
        return v;
    }

    private String makeMatchSummary() {
        int gap = Math.abs(scoreA - scoreB);
        String leader = (scoreA == scoreB) ? "Match level." : (scoreA > scoreB ? teamA : teamB) + " lead by " + gap + ".";
        return "AI Match Summary\n" + leader + "\nPlayer to watch: " + topPlayer();
    }

    private String topPlayer() {
        if (players.isEmpty()) return "None";
        String top = players.get(0);
        int best = playerPoints.containsKey(top) ? playerPoints.get(top) : 0;
        for (String p : players) {
            int val = playerPoints.containsKey(p) ? playerPoints.get(p) : 0;
            if (val > best) { best = val; top = p; }
        }
        return top + " (" + best + " pts)";
    }

    private void shareScorecard() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "Grama-Kalyana Sports Scorecard");
        send.putExtra(Intent.EXTRA_TEXT, makeMatchSummary());
        startActivity(Intent.createChooser(send, "Share"));
    }

    private void loadState() {
        sport = prefs.getString("sport", sport);
        teamA = prefs.getString("teamA", teamA);
        teamB = prefs.getString("teamB", teamB);
        schedule = prefs.getString("schedule", schedule);
        scoreA = prefs.getInt("scoreA", 0);
        scoreB = prefs.getInt("scoreB", 0);
        wicketsA = prefs.getInt("wicketsA", 0);
        wicketsB = prefs.getInt("wicketsB", 0);
        oversA = prefs.getInt("oversA", 0);
        oversB = prefs.getInt("oversB", 0);
        activeTeam = prefs.getInt("activeTeam", 0);
        manOfMatch = prefs.getString("motm", "Not selected");
        currentTab = prefs.getString("currentTab", "setup");
        applyPlayers(prefs.getString("players", "Ravi\nMeena\nArjun\nKavya\nNikhil\nSana"));
        loadPlayerPoints();
    }

    private void saveState() {
        prefs.edit().putString("sport", sport).putString("teamA", teamA).putString("teamB", teamB)
                .putString("schedule", schedule).putInt("scoreA", scoreA).putInt("scoreB", scoreB)
                .putInt("wicketsA", wicketsA).putInt("wicketsB", wicketsB).putInt("oversA", oversA)
                .putInt("oversB", oversB).putInt("activeTeam", activeTeam).putString("motm", manOfMatch)
                .putString("currentTab", currentTab).putString("players", playersAsText())
                .putString("points", pointsAsText()).apply();
    }

    private void applyPlayers(String raw) {
        players.clear();
        for (String line : raw.split("\\n")) {
            String name = line.trim();
            if (name.length() > 0 && !players.contains(name)) players.add(name);
        }
        if (players.isEmpty()) { players.add("Player 1"); players.add("Player 2"); }
        if (!players.contains(manOfMatch)) manOfMatch = "Not selected";
    }

    private void loadPlayerPoints() {
        String raw = prefs.getString("points", "");
        for (String entry : raw.split("\\|")) {
            String[] parts = entry.split("=", 2);
            if (parts.length == 2) {
                try { playerPoints.put(parts[0], Integer.parseInt(parts[1])); } catch (Exception ignored) {}
            }
        }
    }

    private String pointsAsText() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : playerPoints.entrySet()) {
            if (sb.length() > 0) sb.append("|");
            sb.append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.toString();
    }

    private String playersAsText() {
        StringBuilder sb = new StringBuilder();
        for (String p : players) { if (sb.length() > 0) sb.append("\n"); sb.append(p); }
        return sb.toString();
    }

    private void heading(String text) {
        TextView v = new TextView(this); v.setText(text); v.setTextColor(Color.WHITE);
        v.setTextSize(24); v.setTypeface(Typeface.DEFAULT_BOLD); v.setPadding(0, dp(8), 0, dp(6));
        content.addView(v, new LinearLayout.LayoutParams(-1, -2));
    }

    private void label(String text) {
        TextView v = new TextView(this); v.setText(text); v.setTextColor(Color.rgb(255, 212, 0));
        v.setTextSize(14); v.setTypeface(Typeface.DEFAULT_BOLD); v.setPadding(0, dp(12), 0, dp(4));
        content.addView(v, new LinearLayout.LayoutParams(-1, -2));
    }

    private void small(String text) {
        TextView v = new TextView(this); v.setText(text); v.setTextColor(Color.LTGRAY);
        v.setTextSize(15); v.setPadding(0, 0, 0, dp(8));
        content.addView(v, new LinearLayout.LayoutParams(-1, -2));
    }

    private TextView card() {
        TextView v = new TextView(this); v.setTextColor(Color.BLACK);
        v.setBackgroundColor(Color.rgb(255, 212, 0)); v.setPadding(dp(16), dp(14), dp(16), dp(14));
        v.setTextSize(17); v.setLineSpacing(dp(2), 1.0f); return v;
    }

    private EditText input(String value, String hint) {
        EditText e = new EditText(this); e.setText(value); e.setHint(hint);
        e.setTextColor(Color.BLACK); e.setBackgroundColor(Color.WHITE);
        e.setPadding(dp(12), dp(10), dp(12), dp(10)); return e;
    }

    private Button button(String text, int size) {
        Button b = new Button(this); b.setText(text); b.setTextSize(size);
        b.setAllCaps(false); b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(Color.BLACK); b.setBackgroundColor(Color.rgb(255, 212, 0)); return b;
    }

    private LinearLayout.LayoutParams blockParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(5), 0, dp(5)); return p;
    }

    private String cleanOrDefault(String v, String d) { return v.trim().isEmpty() ? d : v.trim(); }

    private int indexOf(String[] arr, String t) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(t)) return i; return 0;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
}
