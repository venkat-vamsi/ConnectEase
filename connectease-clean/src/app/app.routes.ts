import { Routes } from '@angular/router';
import { HomeComponent } from './features/public/home/home';
import { LoginComponent } from './features/auth/login/login';
import { SignupComponent } from './features/auth/signup/signup';
import { ListingsComponent } from './features/public/listings/listings';
import { ServiceDetailsComponent } from './features/public/service-details/service-details';
import { CommunityComponent } from './features/public/community/community';
import { DashboardComponent } from './features/vendor/dashboard/dashboard';
import { AiChatComponent } from './features/ai-chat.component/ai-chat.component'; // NEW

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'services', component: ListingsComponent }, // Keeping this if you still want a traditional directory
  { path: 'ai-chat', component: AiChatComponent },    // NEW AI PAGE
  { path: 'services/:id', component: ServiceDetailsComponent },
  { path: 'community', component: CommunityComponent },
  { path: 'vendor/dashboard', component: DashboardComponent },
  { path: '**', redirectTo: '' }
];