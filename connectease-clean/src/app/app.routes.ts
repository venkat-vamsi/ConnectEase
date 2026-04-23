import { Routes } from '@angular/router';
import { HomeComponent } from './features/public/home/home';
import { LoginComponent } from './features/auth/login/login';
import { SignupComponent } from './features/auth/signup/signup';
import { ListingsComponent } from './features/public/listings/listings';
import { ServiceDetailsComponent } from './features/public/service-details/service-details';
import { CommunityComponent } from './features/public/community/community';
import { DashboardComponent } from './features/vendor/dashboard/dashboard';
import { AiChatComponent } from './features/ai-chat.component/ai-chat.component';
import { ProfileComponent } from './features/profile/profile';
import { ChatHistoryComponent } from './features/chat-history/chat-history';
import { authGuard } from './core/guards/auth.guard';
import { vendorGuard } from './core/guards/vendor.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'services', component: ListingsComponent },
  { path: 'services/:id', component: ServiceDetailsComponent },
  { path: 'ai-chat', component: AiChatComponent },
  { path: 'community', component: CommunityComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: 'chats', component: ChatHistoryComponent, canActivate: [authGuard] },
  { path: 'vendor/dashboard', component: DashboardComponent, canActivate: [vendorGuard] },
  { path: '**', redirectTo: '' }
];
