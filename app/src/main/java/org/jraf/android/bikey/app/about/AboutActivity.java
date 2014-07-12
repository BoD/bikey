/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 * 
 * Copyright (C) 2013 Benoit 'BoD' Lubek (BoD@JRAF.org)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.bikey.app.about;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import org.acra.ACRA;
import org.jraf.android.bikey.BuildConfig;
import org.jraf.android.bikey.R;

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        ((TextView) findViewById(R.id.txtInfo1)).setText(Html.fromHtml(getString(R.string.about_txtInfo1, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE,
                BuildConfig.BUILD_DATE, BuildConfig.GIT_SHA1)));
        findViewById(R.id.btnShare).setOnClickListener(mShareOnClickListener);
        findViewById(R.id.btnRate).setOnClickListener(mRateOnClickListener);
        findViewById(R.id.btnOtherApps).setOnClickListener(mOtherAppsOnClickListener);
        findViewById(R.id.btnDonate).setOnClickListener(mDonateOnClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_sendLogs:
                ACRA.getErrorReporter().handleSilentException(new Exception("User clicked on 'Send logs'"));
                Toast.makeText(this, R.string.about_sendingLogsToast, Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final OnClickListener mShareOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_shareText_subject));
            String shareTextBody = getString(R.string.about_shareText_body, getPackageName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareTextBody);
            shareIntent.putExtra("sms_body", shareTextBody);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.common_shareWith)));
        }
    };

    private final OnClickListener mRateOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + getPackageName()));
            startActivity(Intent.createChooser(intent, null));
        }
    };

    private final OnClickListener mOtherAppsOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://search?q=pub:BoD"));
            startActivity(Intent.createChooser(intent, null));
        }
    };

    private final OnClickListener mDonateOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=BoD%40JRAF%2eorg&lc=US&item_name=Donate%20to%20BoD" +
                    "&item_number=Donate%20to%20BoD&no_note=0&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest" +
                    getPackageName()));
            startActivity(Intent.createChooser(intent, null));
        }
    };
}
